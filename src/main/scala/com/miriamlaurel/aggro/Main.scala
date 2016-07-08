package com.miriamlaurel.aggro

import java.io.File

import com.miriamlaurel.aggro.csv.okcoin.CsvLoader
import com.miriamlaurel.aggro.db.DbLink
import scalikejdbc.config.DBs

object Main extends App {
  if (args.length > 0) {
    DBs.setupAll()
    val dir = new File(args(0))
    val fills = CsvLoader.loadFromCsv(dir)
    DbLink.persistFills(fills.iterator)
    DBs.closeAll()
  } else {
    throw new RuntimeException("Oh shi! " + args)
  }
}
