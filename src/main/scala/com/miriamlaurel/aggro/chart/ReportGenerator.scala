package com.miriamlaurel.aggro.chart

import java.io.{File, FileWriter, InputStreamReader, StringWriter}
import java.nio.file.Files
import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneId}

import com.miriamlaurel.aggro.Inventory
import com.miriamlaurel.aggro.db.DbLink
import com.miriamlaurel.aggro.model.Fill
import com.miriamlaurel.fxcore.instrument.{CurrencyPair, Instrument}
import com.miriamlaurel.fxcore.party.Party
import org.apache.commons.io.IOUtils

import scala.math.BigDecimal.RoundingMode

class ReportGenerator {

  type Report = Seq[(Instant, BigDecimal, BigDecimal)]

  def mkReport(fills: Seq[Fill], pivotPrice: BigDecimal): Report = {
    var total = BigDecimal(0)
    val first = fills.head
    val instrument: Instrument = first.position.instrument
    var inv = first.inventory.get
    val firstInv = inv
    var nav = getNav(instrument, inv, first.position.price)
    for (t <- fills) yield {
      val p = t.position
      inv = t.inventory match {
        case Some(newInv) => newInv
        case None => Map(instrument.base -> (inv(instrument.base) + p.primary.amount), instrument.counter -> (inv(instrument.counter) + p.secondary.amount))
      }
      val nowNav = getNav(instrument, inv, p.price)
      val delta = nowNav - nav
      nav = nowNav
      total = total + delta
      val dPrice = (p.price - first.position.price) * firstInv(instrument.base)
      (p.timestamp, total * pivotPrice, dPrice * pivotPrice)
    }
  }

  def getNav(instrument: Instrument, inv: Inventory, price: BigDecimal): BigDecimal = {
    inv(instrument.base) * price + inv(instrument.counter)
  }
}

object ReportGenerator extends App {

  val STRATEGY = "CNC"
  val VENUE = Party("OKCN")
  val INSTRUMENT = CurrencyPair("BTC/CNY")
  val PIVOT_RATE = BigDecimal("0.149602")
  val SCALE = BigDecimal("9")

  DbLink.initialize()
  val dft = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("UTC"))
  val sft = DateTimeFormatter.ofPattern("MMMM dd").withZone(ZoneId.of("UTC"))
  val repGen = new ReportGenerator
  val fills = DbLink.loadFillsFromDb(VENUE, INSTRUMENT)
  val report = repGen.mkReport(fills, PIVOT_RATE)
  val csv = report.map(e => {
    val timeString = dft.format(e._1)
    val total = e._2.setScale(2, RoundingMode.HALF_EVEN)
    val buyHold = e._3.setScale(2, RoundingMode.HALF_EVEN)
    s"$timeString,$total,$buyHold"
  })
  DbLink.release()
  val writer = new StringWriter()
  val templateReader = new InputStreamReader(classOf[ReportGenerator].getResourceAsStream("/plchart.gnuplot"))
  IOUtils.copy(templateReader, writer)
  templateReader.close()
  val template = writer.toString

  val startDate = report.head._1
  val endDate = report.last._1

  val dataFile = writeDataFile(csv)
  val plotScript = mkPlotScript(template, dataFile, startDate, endDate, STRATEGY, VENUE, SCALE)
  val plotFile = writePlotScript(plotScript)
  val pngFile = Files.createTempFile(null, ".png").toFile

  val pb = new ProcessBuilder("gnuplot", plotFile.getAbsolutePath)
  pb.redirectOutput(pngFile)
  pb.start()

  println(pngFile.getAbsolutePath)
  Thread.sleep(700)
  dataFile.deleteOnExit()
  plotFile.deleteOnExit()


  def writeDataFile(lines: Seq[String]): File = {
    val tmpDataFile = Files.createTempFile(null, null).toFile
    val writer = new FileWriter(tmpDataFile)
    IOUtils.write(lines.mkString("\n"), writer)
    writer.close()
    tmpDataFile
  }

  def writePlotScript(plotScript: String): File = {
    val tmpScriptFile = Files.createTempFile(null, null).toFile
    val scriptWriter = new FileWriter(tmpScriptFile)
    IOUtils.write(plotScript, scriptWriter)
    scriptWriter.close()
    tmpScriptFile
  }

  def mkPlotScript(template: String, dataFile: File, startDate: Instant, endDate: Instant, strategy: String, venue: Party, scale: BigDecimal): String = {
    val start = dft.format(startDate).replaceFirst("\\s.+", " 00:00:00")
    val end = dft.format(endDate).replaceFirst("\\s.+", " 23:59:59")
    template
      .replaceFirst("%START_TIME", start)
      .replaceFirst("%END_TIME", end)
      .replaceFirst("%STRATEGY", strategy)
      .replaceFirst("%VENUE", venue.id)
      .replaceFirst("%START_DATE", sft.format(startDate))
      .replaceFirst("%END_DATE", sft.format(endDate))
      .replaceAll("%SCALE", scale.toString())
      .replaceAll("%DATA_FILE", dataFile.getAbsolutePath)
  }
}
