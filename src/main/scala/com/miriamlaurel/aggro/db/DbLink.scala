package com.miriamlaurel.aggro.db

import java.util.UUID

import com.miriamlaurel.aggro.Inventory
import com.miriamlaurel.aggro.model.Fill
import com.miriamlaurel.fxcore.Monetary
import com.miriamlaurel.fxcore.instrument.Instrument
import com.miriamlaurel.fxcore.party.Party
import com.miriamlaurel.fxcore.portfolio.Position
import com.typesafe.config.Config
import scalikejdbc._

class DbLink(config: Config) {

  val init = new DbInit(config)

  def initialize(): Unit = {
    init.setupAll()
  }

  def release(): Unit = {
    init.closeAll()
  }

  //noinspection RedundantBlock
  def persistFills(fills: Iterator[Fill]): Unit = DB localTx { implicit session =>
    for (t <- fills) {
      persistFill(t)
    }
  }

  //noinspection RedundantBlock
  def persistFill(fill: Fill): Unit = DB localTx { implicit session =>
    val p: Position = fill.position
    val venue = fill.venue
    val id = fill.orderId
    val fillId = fill.fillId
    val time = p.timestamp
    val baseDelta = p.primary.amount
    val quoteDelta = p.secondary.amount
    val instrument: Instrument = p.instrument
    val newBase = for (i <- fill.inventory) yield i(instrument.base)
    val newQuote = for (i <- fill.inventory) yield i(instrument.counter)
    val sql = sql"""INSERT INTO fills (order_id, fill_id, venue, exec_time, base_delta, quote_delta, base_balance, quote_balance, authoritative) VALUES (${id}, ${fillId}, ${venue.id}, ${time}, ${baseDelta}, ${quoteDelta}, ${newBase}, ${newQuote}, TRUE) ON CONFLICT DO NOTHING"""
    sql.update().apply()
  }

  def loadFillsFromDb(venue: Party, instrument: Instrument): Seq[Fill] = DB readOnly { implicit session =>
    val sql = sql"""SELECT distinct on (date_trunc('minute', exec_time)) order_id, fill_id, exec_time, base_asset, quote_asset, base_balance, quote_balance, base_delta, quote_delta FROM fills WHERE venue = ${venue.id} order by date_trunc('minute', exec_time), exec_time ASC"""
    val mapped = sql.map(rs => {
      val id = rs.string("order_id")
      val fillId = rs.string("fill_id")
      val time = rs.timestamp("exec_time").toInstant.toEpochMilli
      val baseDelta = Monetary(rs.double("base_delta"), instrument.base)
      val quoteDelta = Monetary(rs.double("quote_delta"), instrument.counter)
      val baseBalance = rs.doubleOpt("base_balance")
      val quoteBalance = rs.doubleOpt("quote_balance")
      val inv: Option[Inventory] = for (bBase <- baseBalance; bQuote <- quoteBalance) yield Map(instrument.base -> bBase, instrument.counter -> bQuote)
      Fill(venue, Position(baseDelta, quoteDelta, None, time, UUID.randomUUID()), id, if (fillId.isEmpty) None else Some(fillId), inv)
    })
    mapped.list().apply()
  }
}
