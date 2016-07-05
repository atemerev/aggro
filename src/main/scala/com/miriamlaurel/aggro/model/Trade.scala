package com.miriamlaurel.aggro.model

import com.miriamlaurel.aggro.Inventory
import com.miriamlaurel.fxcore.portfolio.Position

case class Trade(fill: Position, orderId: String, fillId: Option[String], inventory: Option[Inventory] = None)