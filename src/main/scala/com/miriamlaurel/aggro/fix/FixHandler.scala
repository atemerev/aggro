package com.miriamlaurel.aggro.fix

import quickfix.MessageCracker.Handler
import quickfix.{Application, Message, SessionID}
import quickfix.fix44.{MessageCracker, TradeCaptureReport}

class FixHandler extends MessageCracker with Application {
  override def fromAdmin(message: Message, sessionId: SessionID): Unit = ???

  override def onLogon(sessionId: SessionID): Unit = ???

  override def onLogout(sessionId: SessionID): Unit = ???

  override def toApp(message: Message, sessionId: SessionID): Unit = ???

  override def onCreate(sessionId: SessionID): Unit = ???

  override def fromApp(message: Message, sessionId: SessionID): Unit = crack(message, sessionId)

  override def toAdmin(message: Message, sessionId: SessionID): Unit = ???

  @Handler
  def onExec(message: TradeCaptureReport, sid: SessionID): Unit = ???
}
