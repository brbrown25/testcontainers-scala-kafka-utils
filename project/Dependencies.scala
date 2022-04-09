import sbt._

object Dependencies {
  object Versions {
    val cats = "2.7.0"
    val catsEffect = "3.3.5"
    val fs2 = "3.2.4"
    val fs2Kafka = "2.3.0"
    val kafkaClients = "3.1.0"
    val munit = "0.7.29"
    val munitCatsEffect = "1.0.7"
    val slf4j = "1.7.35"
    val testcontainers = "0.40.4"
  }

  lazy val catsCore = "org.typelevel" %% "cats-core" % Versions.cats
  lazy val catsEffect = "org.typelevel" %% "cats-effect" % Versions.catsEffect
  lazy val catsEffectKernel = "org.typelevel" %% "cats-effect-kernel" % Versions.catsEffect
  lazy val catsFree = "org.typelevel" %% "cats-free" % Versions.cats
  lazy val catsKernel = "org.typelevel" %% "cats-kernel" % Versions.cats
  lazy val fs2Core = "co.fs2" %% "fs2-core" % Versions.fs2
  lazy val fs2Kafka = "com.github.fd4s" %% "fs2-kafka" % Versions.fs2Kafka
  lazy val kafkaClients = "org.apache.kafka" % "kafka-clients" % Versions.kafkaClients
  lazy val munit = "org.scalameta" %% "munit" % Versions.munit % Test
  lazy val munitCatsEffect = "org.typelevel" %% "munit-cats-effect-3" % Versions.munitCatsEffect
  lazy val munitScalaCheck = "org.scalameta" %% "munit-scalacheck" % Versions.munit
  lazy val slf4jApi = "org.slf4j" % "slf4j-api" % Versions.slf4j
  lazy val testcontainersKafka = "com.dimafeng" %% "testcontainers-scala-kafka" % Versions.testcontainers
  lazy val testcontainersMunit = "com.dimafeng" %% "testcontainers-scala-munit" % Versions.testcontainers

  lazy val testDeps = Seq(
    munit,
    munitCatsEffect,
    munitScalaCheck,
    testcontainersKafka,
    testcontainersMunit,
  ).map(_ % "test,it")
}
