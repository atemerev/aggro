package com.miriamlaurel.aggro.fix

import java.time.Instant
import java.util.UUID

import com.miriamlaurel.aggro.model.Fill
import com.miriamlaurel.fxcore.instrument.CurrencyPair
import com.miriamlaurel.fxcore.party.Party
import com.miriamlaurel.fxcore.portfolio.{Position, PositionSide}
import quickfix.MessageCracker.Handler
import quickfix.field.Side
import quickfix.{Application, Message, SessionID, fix44}
import quickfix.fix44.{MessageCracker, TradeCaptureReport}

class FixHandler(reportListener: Function[Fill, Unit]) extends MessageCracker with Application {
  override def fromAdmin(message: Message, sessionId: SessionID): Unit = ???

  override def onLogon(sessionId: SessionID): Unit = ???

  override def onLogout(sessionId: SessionID): Unit = ???

  override def toApp(message: Message, sessionId: SessionID): Unit = ???

  override def onCreate(sessionId: SessionID): Unit = ???

  override def fromApp(message: Message, sessionId: SessionID): Unit = crack(message, sessionId)

  override def toAdmin(message: Message, sessionId: SessionID): Unit = ???

  @Handler
  def onExec(message: TradeCaptureReport, sid: SessionID): Unit = {
    val symbol = message.getSymbol.getValue
    val instrument = CurrencyPair(symbol)
    val tradeQty = BigDecimal.valueOf(message.getLastQty.getValue)
    val price = BigDecimal.valueOf(message.getLastPx.getValue)
    val sideGrp = new fix44.TradeCaptureReport.NoSides
    message.getGroup(1, sideGrp)
    val side = if (sideGrp.getSide.getValue == Side.BUY) PositionSide.Long else PositionSide.Short
    val orderId = sideGrp.getOrderID.getValue
    val tradeId = message.getExecID.getValue
    val position = Position(instrument, price, if (side == PositionSide.Long) tradeQty else -tradeQty, None, Instant.now(), UUID.randomUUID())
    val venue = Party("OKCN") // todo extract from repeating group
    val fill = Fill(venue, position, orderId, Some(tradeId), None) // todo populate inventory
    reportListener.apply(fill)
  }

}
