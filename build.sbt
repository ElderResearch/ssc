name := "Simple Scala Config"

moduleName := "ssc"

version := "0.2.1-SNAPSHOT"

organization := "com.elderresearch"

licenses := Seq(("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html")))

scalaVersion := "2.11.8"

scalacOptions += "-target:jvm-1.8"

libraryDependencies ++= Seq(
  "org.scala-lang" %  "scala-reflect"  % scalaVersion.value,
  "com.typesafe" % "config" % "1.3.0",
  "org.scalatest" %% "scalatest" % "2.2.6" % "test"
)

publishMavenStyle := false

bintrayOrganization := Some("elderresearch")

bintrayRepository := "OSS"

bintrayReleaseOnPublish in ThisBuild := false

tutSettings

tutTargetDirectory := baseDirectory.value