lazy val Scala212         = "2.12.17"
lazy val Scala213         = "2.13.10"
lazy val Scala3           = "3.2.2"
lazy val allCrossVersions = Seq(Scala212, Scala213, Scala3)

ThisBuild / organization := "com.bbrownsound"
ThisBuild / scalaVersion := Scala213
ThisBuild / crossScalaVersions := allCrossVersions

Global / onChangedBuildSource := ReloadOnSourceChanges

def adopt(version: String): JavaSpec = JavaSpec(JavaSpec.Distribution.Adopt, version)

ThisBuild / githubWorkflowJavaVersions := Seq(adopt("8"), adopt("11"), adopt("17"))
ThisBuild / githubWorkflowArtifactUpload := false
ThisBuild / githubWorkflowAddedJobs ++= Seq(
  WorkflowJob(
    "formatting",
    "Check formatting",
    githubWorkflowJobSetup.value.toList ::: List(
      // do to some quirks with scalafix and scala3 use scala 2.13 for the formatting
      WorkflowStep
        .Run(List(s"sbt ++$Scala213 checkAll"), name = Some("Check formatting"))
    )
  )
)
ThisBuild / githubWorkflowPublishTargetBranches :=
  Seq(
    RefPredicate.StartsWith(Ref.Tag("*")),
    RefPredicate.Contains(Ref.Branch("master")),
    RefPredicate.Contains(Ref.Branch("main"))
  )

// ThisBuild / githubWorkflowPublish := Seq(
//   WorkflowStep.Sbt(
//     List("ci-release"),
//     env = Map(
//       "PGP_PASSPHRASE" -> "${{ secrets.PGP_PASSPHRASE }}",
//       "PGP_SECRET" -> "${{ secrets.PGP_SECRET }}",
//       "SONATYPE_PASSWORD" -> "${{ secrets.SONATYPE_PASSWORD }}",
//       "SONATYPE_USERNAME" -> "${{ secrets.SONATYPE_USERNAME }}"
//     )
//   )
// )

lazy val core = (project in file("./modules/core"))
  .settings(
    name := "testcontainers-scala-kafka-utils-core",
    (publish / skip) := false,
    Release.settings,
    libraryDependencies ++= (Dependencies.coreDeps ++ Dependencies.testDeps)
  )

lazy val root = project
  .in(file("."))
  .settings(
    name := "testcontainers-scala-kafka-utils",
    (publish / skip) := true
  )
  .aggregate(core)

inThisBuild(
  List(
    organization := "com.bbrownsound",
    homepage := Some(url("https://github.com/brbrown25/testcontainers-scala-kafka-utils")),
    licenses := List("MIT" -> url("https://github.com/brbrown25/testcontainers-scala-kafka-utils/blob/main/LICENSE")),
    developers := List(
      Developer(
        "brbrown25",
        "Brandon Brown",
        "brandon@bbrownsound.com",
        url("https://bbrownsound.com")
      )
    ),
    semanticdbEnabled := true, // enable SemanticDB
    semanticdbVersion := scalafixSemanticdb.revision, // use Scalafix compatible version
    scalafixScalaBinaryVersion := CrossVersion.binaryScalaVersion(scalaVersion.value),
    scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.6.0",
    scalacOptions ++= Seq("-Wconf:origin=scala.collection.compat.*:s", "-Ywarn-unused")
  )
)

addCommandAlias("fix", "scalafixAll")
addCommandAlias("fixCheck", "scalafixAll --check")
addCommandAlias("fmt", "scalafmtSbt; scalafmtAll")
addCommandAlias("fmtCheck", "scalafmtSbtCheck; scalafmtCheckAll")
addCommandAlias("prepare", "fix; fmt; githubWorkflowGenerate")
addCommandAlias("checkAll", "fixCheck; fmtCheck; githubWorkflowCheck")
