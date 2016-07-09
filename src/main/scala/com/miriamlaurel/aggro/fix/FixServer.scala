package com.miriamlaurel.aggro.fix

import akka.actor.{Actor, ActorSystem, Props}
import com.miriamlaurel.aggro.model.Fill
import quickfix._

import scala.concurrent.Promise

class FixServer extends Actor {
  val app = new FixHandler(self ! _)
  val settings = new SessionSettings(FixServer.this.getClass.getResourceAsStream("/aggro-session.ini"))
  val sessionFactory: SessionFactory = new DefaultSessionFactory(app, new MemoryStoreFactory, new FileLogFactory(settings), new quickfix.DefaultMessageFactory)
  val acceptor = new SocketAcceptor(app, new MemoryStoreFactory, settings, new FileLogFactory(settings), new DefaultMessageFactory)
  val sessionPromise: Promise[SessionID] = Promise()

  override def preStart(): Unit = {
    acceptor.start()
  }

  override def receive = {
    case fill: Fill => println(fill)
  }

  override def postStop(): Unit = {
    acceptor.stop()
  }
}

object FixServer extends App {
  val system = ActorSystem("fix")
  system.actorOf(Props[FixServer])
}
