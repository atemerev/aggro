mainClass in Compile := Some("com.miriamlaurel.aggro.chart.ReportGenerator")

lazy val root = (project in file(".")).
  settings(
    name := "aggro",
    organization := "com.miriamlaurel",
    scalaVersion := "2.11.8",
    version := "0.1.0",
    sbtVersion := "0.13.12",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % "2.4.12",
      "com.typesafe.akka" %% "akka-stream" % "2.4.12",
      "com.typesafe.akka" %% "akka-http-core" % "3.0.0-RC1",
      "org.quickfixj" % "quickfixj-all" % "1.6.2",
      "com.miriamlaurel" %% "fxcore" % "2.4-SNAPSHOT",
      "org.json4s" %% "json4s-native" % "3.3.0",
      "ch.qos.logback" %  "logback-classic" % "1.1.7",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.4.0",
      "org.scalikejdbc" %% "scalikejdbc" % "2.4.1",
      "com.github.tototoshi" %% "scala-csv" % "1.3.3",
      "org.scalikejdbc" %% "scalikejdbc-config"  % "2.4.1",
      "org.postgresql" % "postgresql" % "9.4.1212",
      "org.apache.commons" % "commons-io" % "1.3.2",
      "org.scalatest" %% "scalatest" % "3.0.0" % "test"
    )
  ).enablePlugins(JavaAppPackaging)
