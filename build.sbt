mainClass in Compile := Some("com.miriamlaurel.aggro.chart.ReportGenerator")

lazy val root = (project in file(".")).
  settings(
    name := "aggro",
    organization := "com.miriamlaurel",
    scalaVersion := "2.12.0",
    version := "0.1.0",
    sbtVersion := "0.13.13",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % "2.4.14",
      "com.typesafe.akka" %% "akka-stream" % "2.4.14",
      "com.typesafe.akka" %% "akka-http-core" % "10.0.0",
      "org.quickfixj" % "quickfixj-all" % "1.6.2",
      "com.miriamlaurel" %% "fxcore" % "2.4-SNAPSHOT",
      "org.json4s" %% "json4s-native" % "3.5.0",
      "ch.qos.logback" %  "logback-classic" % "1.1.7",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
      "org.scalikejdbc" %% "scalikejdbc" % "3.0.0-M1",
      "com.github.tototoshi" %% "scala-csv" % "1.3.4",
      "org.scalikejdbc" %% "scalikejdbc-config"  % "3.0.0-M1",
      "org.postgresql" % "postgresql" % "9.4.1212",
      "org.apache.commons" % "commons-io" % "1.3.2",
      "org.scalatest" %% "scalatest" % "3.0.1" % "test"
    )
  ).enablePlugins(JavaAppPackaging)
