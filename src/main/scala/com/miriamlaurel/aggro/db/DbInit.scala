package com.miriamlaurel.aggro.db

import com.typesafe.config.Config
import scalikejdbc.config.{DBs, NoEnvPrefix, TypesafeConfig, TypesafeConfigReader}

class DbInit(override val config: Config) extends DBs with TypesafeConfigReader with NoEnvPrefix with TypesafeConfig