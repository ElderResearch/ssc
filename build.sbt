name := "Simple Scala Config"

moduleName := "ssc"

version := "0.1.0-SNAPSHOT"

organization := "com.elderresearch"

scalaVersion := "2.11.8"

scalacOptions += "-target:jvm-1.7"

libraryDependencies ++= Seq(
  "org.scala-lang" %  "scala-reflect"  % scalaVersion.value,
  "com.typesafe" % "config" % "1.3.0",
  "org.scalatest" %% "scalatest" % "2.2.6" % "test"
)
