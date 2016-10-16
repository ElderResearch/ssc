
name := "Simple Scala Config"

moduleName := "ssc"

version := "1.0.0"

organization := "com.elderresearch"

licenses := Seq(("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html")))

scalaVersion := "2.11.8"

crossScalaVersions := Seq("2.11.8", "2.12.0-RC1")

libraryDependencies ++= Seq(
  "org.scala-lang" %  "scala-reflect"  % scalaVersion.value,
  "com.typesafe" % "config" % "1.3.1",
  "org.scalatest" % "scalatest" % "3.0.0" cross CrossVersion.binary
)

publishMavenStyle := true

bintrayOrganization := Some("elderresearch")

bintrayRepository := "OSS"

bintrayReleaseOnPublish in ThisBuild := false

tutSettings

tutTargetDirectory := baseDirectory.value
