package com.miriamlaurel.aggro

import java.io.File

import com.miriamlaurel.aggro.csv.okcoin.CsvLoader
import com.miriamlaurel.aggro.db.DbLink
import scalikejdbc.config.DBs

object Main extends App {
  if (args.length > 0) {
    val db = new DbLink("fills")
    db.initialize()
    val dir = new File(args(0))
    val fills = CsvLoader.loadFromCsv(dir)
    db.persistFills(fills.iterator)
    db.release()
  } else {
    throw new RuntimeException("Oh shi! " + args)
  }
}
