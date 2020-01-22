name := """shijianji"""
organization := "it.softfork"

version := "1.0-SNAPSHOT"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)

scalaVersion := "2.13.1"

libraryDependencies ++= Seq(
  // Dependency Injection
  guice,
  "net.codingwell" %% "scala-guice" % "4.2.6",
  // Test
  "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test,
  // Logging
  "net.logstash.logback" % "logstash-logback-encoder" % "6.2",
  // Helpers
  "io.lemonlabs" %% "scala-uri" % "1.5.1",
  // Database
  "com.typesafe.play" %% "play-slick" % "5.0.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "5.0.0",
  "com.github.tminglei" %% "slick-pg" % "0.18.1", // postgresql extensions
  "com.github.tminglei" %% "slick-pg_play-json" % "0.18.1" // play-json support
)

scalacOptions ++=  Seq(
  "-feature",
  "-deprecation",
  "-Xfatal-warnings"
)

// Use different configuration file for test
javaOptions in Test += "-Dconfig.file=conf/application.test.conf"

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "it.softfork.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "it.softfork.binders._"
