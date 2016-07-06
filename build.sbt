mainClass in Compile := Some("com.miriamlaurel.carb.Main")

lazy val root = (project in file(".")).
  settings(
    name := "aggro",
    organization := "com.miriamlaurel",
    scalaVersion := "2.11.8",
    version := "0.1.0",
    sbtVersion := "0.13.11",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % "2.4.7",
      "com.typesafe.akka" %% "akka-stream" % "2.4.7",
      "com.typesafe.akka" %% "akka-http-core" % "2.4.7",
      "org.quickfixj" % "quickfixj-all" % "1.6.2",
      "com.miriamlaurel" %% "fxcore" % "2.2-SNAPSHOT",
      "org.json4s" %% "json4s-native" % "3.3.0",
      "ch.qos.logback" %  "logback-classic" % "1.1.7",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.4.0",
      "org.scalikejdbc" %% "scalikejdbc" % "2.4.1",
      "com.github.tototoshi" %% "scala-csv" % "1.3.3",
      "org.scalikejdbc" %% "scalikejdbc-config"  % "2.4.1",
      "org.postgresql" % "postgresql" % "9.4.1208.jre7",
      "org.apache.commons" % "commons-io" % "1.3.2",
      "org.scalatest" %% "scalatest" % "2.2.6" % "test"
    )
  ).enablePlugins(JavaAppPackaging)
