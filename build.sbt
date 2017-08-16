import ReleaseTransformations._
import sbt._

name := "Simple Scala Config"

moduleName := "ssc"

organization := "com.elderresearch"

licenses := Seq(("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html")))

scalaVersion := "2.11.8"

crossScalaVersions := Seq("2.11.8", "2.12.3")

libraryDependencies ++= Seq(
  "org.scala-lang" %  "scala-reflect"  % scalaVersion.value,
  "com.typesafe" % "config" % "1.3.1",
  "org.scalatest" % "scalatest" % "3.0.0" % Test cross CrossVersion.binary
)

publishMavenStyle := true

bintrayOrganization := Some("elderresearch")

bintrayRepository := "OSS"

bintrayReleaseOnPublish in ThisBuild := false

tutSettings

tutTargetDirectory := baseDirectory.value


lazy val runTut = releaseStepTask(tut)
lazy val commitTut = ReleaseStep((st: State) ⇒ {
  val extracted = Project.extract(st)

  val vcs = extracted.get(releaseVcs).get
  vcs.add("README.md")
  val status = vcs.status.!!.trim
  if (status.nonEmpty) {
    vcs.commit("Generated README.md", sign = false)
  }
  st
})

releaseCrossBuild := true

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  runTut,
  commitTut,
  commitReleaseVersion,
  tagRelease,
  publishArtifacts,
  setNextVersion,
  commitNextVersion,
  pushChanges
)
