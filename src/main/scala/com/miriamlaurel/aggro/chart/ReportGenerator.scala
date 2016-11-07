package com.miriamlaurel.aggro.chart

import java.io.{File, FileWriter, InputStreamReader, StringWriter}
import java.nio.file.{Files, StandardCopyOption}
import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneId}

import com.miriamlaurel.aggro.Inventory
import com.miriamlaurel.aggro.db.DbLink
import com.miriamlaurel.aggro.model.Fill
import com.miriamlaurel.fxcore.SafeDouble
import com.miriamlaurel.fxcore.instrument.{CurrencyPair, Instrument}
import com.miriamlaurel.fxcore.party.Party
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.commons.io.IOUtils


class ReportGenerator(config: Config) {

  type Report = Seq[(Long, SafeDouble, SafeDouble, SafeDouble, SafeDouble, SafeDouble)]

  def mkReport(fills: Seq[Fill]): Report = {
    val first = fills.head
    val instrument: Instrument = first.position.instrument
    var inv = first.inventory.get
    val initialPrice = first.position.price
    var initialAmount = inv(instrument.base) + inv(instrument.counter) / initialPrice
    var initialNav = getNav(instrument, inv, initialPrice)
    var prevNav = initialNav
    val data = for (t <- fills) yield {
      val p = t.position
      inv = t.inventory match {
        case Some(newInv) => newInv
        case None => Map(instrument.base -> (inv(instrument.base) + p.primary.amount), instrument.counter -> (inv(instrument.counter) + p.secondary.amount))
      }
      val nowNav = getNav(instrument, inv, p.price)
      var nowDelta = nowNav / prevNav - 1
      if (nowDelta > 0.2) {
        // a hack to account for deposits/withdrawals; todo get them from API
        println(prevNav)
        println(nowNav)
        initialAmount = nowNav / (prevNav / initialNav) / p.price
        initialNav = initialAmount * initialPrice
        nowDelta = nowNav / initialNav - 1
      }
      prevNav = nowNav
      val buyAndHoldNav = p.price * initialAmount
      val buyAndHoldDelta = buyAndHoldNav / initialNav - 1
      val navDelta = nowNav / initialNav - 1
//      println(navDelta)
      (p.timestamp, p.price, nowNav, buyAndHoldNav, navDelta, buyAndHoldDelta)
    }
    data
  }

  def getNav(instrument: Instrument, inv: Inventory, price: SafeDouble): SafeDouble = {
    inv(instrument.base) * price + inv(instrument.counter)
  }
}

object ReportGenerator extends App {

  val configFile = new File("report.conf")
  if (!configFile.canRead) {
    System.err.println("Can't load report.conf configuration file from current directory. Does it exist? Can it be read?")
    System.exit(1)
  }
  val config = ConfigFactory.parseFile(configFile)

  val STRATEGY = config.getString("reporting.strategyName")
  val VENUE = Party(config.getString("reporting.venue"))
  val INSTRUMENT = CurrencyPair(config.getString("reporting.instrument"))
  // start date: 2016-07-06 23:49:00 Asia/Shanghai

  val db = new DbLink(config)
  db.initialize()
  val dft = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("UTC"))
  val sft = DateTimeFormatter.ofPattern("MMMM dd").withZone(ZoneId.of("UTC"))
  val repGen = new ReportGenerator(config)
  val fills = db.loadFillsFromDb(VENUE, INSTRUMENT)
  val fst = fills.head
  val inv = fst.inventory.get
  val report = repGen.mkReport(fills)
  val csv = report.map(e => {
    val timeString = dft.format(Instant.ofEpochMilli(e._1))
    val total = e._5
    val buyHold = e._6
    s"$timeString,$total,$buyHold"
  })
  db.release()
  val writer = new StringWriter()
  val templateReader = new InputStreamReader(classOf[ReportGenerator].getResourceAsStream("/plchart.gnuplot"))
  IOUtils.copy(templateReader, writer)
  templateReader.close()
  val template = writer.toString

  val startDate = report.head._1
  val endDate = report.last._1

  val dataFile = writeDataFile(csv)
  val plotScript = mkPlotScript(template, dataFile, Instant.ofEpochMilli(startDate), Instant.ofEpochMilli(endDate), STRATEGY, VENUE)
  val plotFile = writePlotScript(plotScript)
  val pngFile = Files.createTempFile(null, ".svg").toFile

  val pb = new ProcessBuilder("gnuplot", plotFile.getAbsolutePath)
  pb.redirectOutput(pngFile)
  pb.start()
  Thread.sleep(700)
  println(pngFile.getAbsolutePath)
  Files.copy(pngFile.toPath, new File(config.getString("reporting.outFile")).toPath, StandardCopyOption.REPLACE_EXISTING)
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

  def mkPlotScript(template: String, dataFile: File, startDate: Instant, endDate: Instant, strategy: String, venue: Party): String = {
    val start = dft.format(startDate)
    val end = dft.format(endDate.plusSeconds(600))
    template
      .replaceFirst("%START_TIME", start)
      .replaceFirst("%END_TIME", end)
      .replaceFirst("%STRATEGY", strategy)
      .replaceFirst("%VENUE", venue.id)
      .replaceFirst("%START_DATE", sft.format(startDate))
      .replaceFirst("%END_DATE", sft.format(endDate))
      .replaceAll("%DATA_FILE", dataFile.getAbsolutePath)
  }
}
