package com.miriamlaurel.aggro.model

import com.miriamlaurel.aggro.Inventory
import com.miriamlaurel.fxcore.party.Party
import com.miriamlaurel.fxcore.portfolio.Position

case class Fill(venue: Party, position: Position, orderId: String, fillId: Option[String], inventory: Option[Inventory] = None)