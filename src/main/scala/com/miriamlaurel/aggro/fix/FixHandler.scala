package com.miriamlaurel.aggro.fix

import java.util.UUID

import com.miriamlaurel.aggro.Inventory
import com.miriamlaurel.aggro.model.Fill
import com.miriamlaurel.fxcore.SafeDouble
import com.miriamlaurel.fxcore.asset.Currency
import com.miriamlaurel.fxcore.instrument.CurrencyPair
import com.miriamlaurel.fxcore.party.Party
import com.miriamlaurel.fxcore.portfolio.{Position, PositionSide}
import com.typesafe.scalalogging.LazyLogging
import quickfix.MessageCracker.Handler
import quickfix.field.Side
import quickfix.fix44.TradeCaptureReport
import quickfix.{Application, Message, MessageCracker, SessionID}

class FixHandler(reportListener: Function[Fill, Unit]) extends MessageCracker with Application with LazyLogging {

  // todo: no need to password-protect, just need to configure the firewall to accept only
  // todo: white-listed connections on this port.
  override def fromAdmin(message: Message, sessionId: SessionID): Unit = ()

  override def onLogon(sessionId: SessionID): Unit = {
    logger.info("Logged in: " + sessionId)
  }

  override def onLogout(sessionId: SessionID): Unit = {
    logger.warn("Logged out: " + sessionId)
  }

  override def toApp(message: Message, sessionId: SessionID): Unit = ()

  override def onCreate(sessionId: SessionID): Unit = {
    logger.info("FIX session created: " + sessionId)
  }

  override def fromApp(message: Message, sessionId: SessionID): Unit = crack(message, sessionId)

  override def toAdmin(message: Message, sessionId: SessionID): Unit = ()

  @Handler
  def onExec(message: TradeCaptureReport, sid: SessionID): Unit = {
    val symbol = message.getSymbol.getValue
    val instrument = CurrencyPair(symbol)
    val tradeQty = SafeDouble(message.getLastQty.getValue)
    val price = SafeDouble(message.getLastPx.getValue)
    val sideGrp = new TradeCaptureReport.NoSides
    message.getGroup(1, sideGrp)
    val side = if (sideGrp.getSide.getValue == Side.BUY) PositionSide.Long else PositionSide.Short
    val orderId = sideGrp.getOrderID.getValue
    val partyGrp = new TradeCaptureReport.NoSides.NoPartyIDs
    sideGrp.getGroup(1, partyGrp)
    val party = partyGrp.getPartyID.getValue
    val tradeId = message.getExecID.getValue
    val position = Position(instrument, price, if (side == PositionSide.Long) tradeQty else -tradeQty, None, System.currentTimeMillis(), UUID.randomUUID())
    val venue = Party(party)
    val legGrp = new TradeCaptureReport.NoLegs
    val balance = for (i <- 1 to message.getNoLegs.getValue) yield {
      message.getGroup(i, legGrp)
      val ccy = Currency(legGrp.getLegCurrency.getValue)
      val qty = SafeDouble(legGrp.getLegQty.getValue)
      ccy -> qty
    }
    val inventory: Inventory = balance.toMap
    val fill = Fill(venue, position, orderId, Some(tradeId), Some(inventory))
    reportListener.apply(fill)
  }

}
