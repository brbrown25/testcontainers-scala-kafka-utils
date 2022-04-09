import sbt.Keys._
import sbt._
import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations._
import sbtrelease.ReleasePlugin.autoImport._
import xerial.sbt.Sonatype.SonatypeKeys._

object Release {
  val settings =
    Seq(
      releaseCrossBuild := true,
      sonatypeProfileName := "com.bbrownsound",
      releaseProcess := Seq[ReleaseStep](
        checkSnapshotDependencies,
        inquireVersions,
        runClean,
        runTest,
        setReleaseVersion,
        commitReleaseVersion,
        tagRelease,
        releaseStepCommand("sonatypeOpen"),
        releaseStepCommand("+publishSigned"),
        releaseStepCommand("sonatypeClose"),
        releaseStepCommand("sonatypePromote"),
        setNextVersion,
        commitNextVersion,
        releaseStepCommand("+publishSigned"),
        pushChanges
      )
    )
}
