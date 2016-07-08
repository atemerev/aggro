package com.miriamlaurel.aggro.csv.okcoin

import java.io.{File, FilenameFilter}
import java.time.{Instant, ZoneId}
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor
import java.util.UUID

import com.github.tototoshi.csv.CSVReader
import com.miriamlaurel.aggro.Inventory
import com.miriamlaurel.aggro.model.Fill
import com.miriamlaurel.fxcore._
import com.miriamlaurel.fxcore.asset.Currency
import com.miriamlaurel.fxcore.party.Party
import com.miriamlaurel.fxcore.portfolio.Position

object CsvLoader {

  val oft = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("Asia/Shanghai"))

  def parseCsv(tokens: Seq[String]): Fill = {
    val fillId = tokens.head.toLong
    val id = tokens(1).toLong
    val timeString: String = tokens(2)
    val btcBalance = BigDecimal(tokens(5).split("/")(0).replaceAll(",", ""))
    val cnyBalance = BigDecimal(tokens(7).replaceAll(",", ""))
    val parsedTime: TemporalAccessor = oft.parse(timeString)
    val ts = Instant.from(parsedTime)
    try {
      val btcStr = tokens(4).replaceAll(",", "")
      val btcS = if (btcStr.startsWith("+")) btcStr.substring(1) else btcStr
      val cnyStr = tokens(6).replaceAll(",", "")
      val cnyS = if (cnyStr.startsWith("+")) cnyStr.substring(1) else cnyStr
      val btc: Monetary = Monetary(BigDecimal(btcS), Bitcoin)
      val cny: Monetary = Monetary(BigDecimal(cnyS), Currency("CNY"))
      val position = Position(btc, cny, None, ts, UUID.randomUUID())
      val inventory: Inventory = Map(Bitcoin -> btcBalance, Currency("CNY") -> cnyBalance)
      Fill(Party("OKCN"), position, id.toString, Some(fillId.toString), Some(inventory))
    } catch {
      case x: Throwable =>
        x.printStackTrace()
        throw x
    }

  }

  def loadFromCsv(dir: File): Array[Fill] = {
    val files = dir.listFiles(new FilenameFilter {
      override def accept(dir: File, name: String): Boolean = name.startsWith("Transaction") && name.endsWith(".csv")
    })
    val data = for (file <- files) yield {
      val reader = CSVReader.open(file)
      val fills = reader.toStream.drop(1).map(parseCsv).toArray
      reader.close()
      fills
    }
    data.flatten
  }
}
