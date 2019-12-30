name := """shijianji-play"""
organization := "it.softfork"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.1"

libraryDependencies ++= Seq(
  guice,
  "net.logstash.logback" % "logstash-logback-encoder" % "6.2",
  "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test
)

scalacOptions ++=  Seq(
  "-feature",
  "-deprecation",
  "-Xfatal-warnings"
)

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "it.softfork.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "it.softfork.binders._"
