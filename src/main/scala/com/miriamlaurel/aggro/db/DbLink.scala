package com.miriamlaurel.aggro.db

import java.util.UUID

import com.miriamlaurel.aggro.Inventory
import com.miriamlaurel.aggro.model.{Fill, Venue}
import com.miriamlaurel.fxcore.instrument.Instrument
import com.miriamlaurel.fxcore.portfolio.Position
import com.miriamlaurel.fxcore.{Monetary, _}
import scalikejdbc.config.DBs
import scalikejdbc.{DB, _}

object DbLink {

  def initialize(): Unit = {
    DBs.setupAll()
  }

  def release(): Unit = {
    DBs.closeAll()
  }

  //noinspection RedundantBlock
  def persistFills(venue: Venue, fills: Iterator[Fill]): Unit = DB localTx { implicit session =>
    for (t <- fills) {
      val p: Position = t.fill
      val id = t.orderId
      val fillId = t.fillId
      val time = p.timestamp
      val baseDelta = p.primary.amount
      val quoteDelta = p.secondary.amount
      val instrument: Instrument = p.instrument
      val newBase = for (i <- t.inventory) yield i(instrument.base)
      val newQuote = for (i <- t.inventory) yield i(instrument.counter)
      val sql = sql"""INSERT INTO public.fills (order_id, fill_id, venue, exec_time, base_delta, quote_delta, base_balance, quote_balance, authoritative) VALUES (${id}, ${fillId}, ${venue.ticker}, ${time}, ${baseDelta}, ${quoteDelta}, ${newBase}, ${newQuote}, TRUE) ON CONFLICT DO NOTHING"""
      sql.update().apply()
    }
  }

  def loadFillsFromDb(venue: Venue, instrument: Instrument): Seq[Fill] = DB readOnly { implicit session =>
    val sql = sql"""SELECT order_id, fill_id, exec_time, base_asset, quote_asset, base_balance, quote_balance, base_delta, quote_delta FROM public.fills WHERE venue = ${venue.ticker} ORDER BY exec_time, order_id, fill_id ASC"""
    val mapped = sql.map(rs => {
      val id = rs.string("order_id")
      val fillId = rs.string("fill_id")
      val time = rs.timestamp("exec_time").toInstant
      val baseDelta = Monetary(rs.bigDecimal("base_delta"), instrument.base)
      val quoteDelta = Monetary(rs.bigDecimal("quote_delta"), instrument.counter)
      val baseBalance = rs.bigDecimalOpt("base_balance")
      val quoteBalance = rs.bigDecimalOpt("quote_balance")
      val inv: Option[Inventory] = for (bBase <- baseBalance; bQuote <- quoteBalance) yield Map(instrument.base -> bBase, instrument.counter -> bQuote)
      Fill(Position(baseDelta, quoteDelta, None, time, UUID.randomUUID()), id, if (fillId.isEmpty) None else Some(fillId), inv)
    })
    mapped.list().apply()
  }
}
