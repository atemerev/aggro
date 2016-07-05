package com.miriamlaurel.aggro.db

import java.util.UUID

import com.miriamlaurel.aggro.Inventory
import com.miriamlaurel.aggro.model.Trade
import com.miriamlaurel.fxcore.asset.Currency
import com.miriamlaurel.fxcore.instrument.Instrument
import com.miriamlaurel.fxcore.portfolio.Position
import com.miriamlaurel.fxcore.{Monetary, _}
import scalikejdbc.{DB, _}

object DbLink {
  //noinspection RedundantBlock
  def persistPositions(fills: Iterator[Trade]): Unit = DB localTx { implicit session =>
    for (t <- fills) {
      val p = t.fill
      val id = t.orderId
      val fillId = t.fillId
      val ticker = p.instrument.toString
      val venue = "OKCN"
      val time = p.timestamp
      val btc = p.primary.amount
      val cny = p.secondary.amount
      val newBtc = for (i <- t.inventory) yield i(Bitcoin)
      val newCny = for (i <- t.inventory) yield i(Currency("CNY"))
      val sql = sql"""INSERT INTO public.fills (order_id, fill_id, instrument, venue, exec_time, delta_btc, delta_cny, delta_usd, new_btc, new_cny) VALUES (${id}, ${fillId}, ${ticker}, ${venue}, ${time}, ${btc}, ${cny}, 0, ${newBtc}, ${newCny}) ON CONFLICT DO NOTHING"""
      sql.update().apply()
    }
  }

  def loadPositionsFromDb(instrument: Instrument): Seq[Trade] = DB readOnly { implicit session =>
    val sql = sql"""SELECT order_id, fill_id, instrument, exec_time, delta_btc, delta_cny, new_btc, new_cny FROM public.fills ORDER BY exec_time, order_id, fill_id ASC"""
    val mapped = sql.map(rs => {
      val id = rs.string("order_id")
      val fillId = rs.string("fill_id")
      val time = rs.timestamp("exec_time").toInstant
      val btc = Monetary(rs.bigDecimal("delta_btc"), Bitcoin)
      val cny = Monetary(rs.bigDecimal("delta_cny"), Currency("CNY"))
      val balanceBtc = rs.bigDecimalOpt("new_btc")
      val balanceCny = rs.bigDecimalOpt("new_cny")
      val inv: Option[Inventory] = for (bbtc <- balanceBtc; bcny <- balanceCny) yield Map(Bitcoin -> bbtc, Currency("CNY") -> bcny)
      Trade(Position(btc, cny, None, time, UUID.randomUUID()), id, if (fillId.isEmpty) None else Some(fillId), inv)
    })
    mapped.list().apply()
  }
}
