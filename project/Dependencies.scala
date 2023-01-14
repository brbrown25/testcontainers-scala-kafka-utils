import sbt._

object Dependencies {
  object Versions {
    val cats              = "2.9.0"
    val catsEffect        = "3.4.4"
    val fs2               = "3.2.4"
    val fs2Kafka          = "2.5.0"
    val kafkaClients      = "3.3.1"
    val munit             = "0.7.29"
    val munitCatsEffect   = "1.0.7"
    val slf4j             = "1.7.35"
    val testcontainers    = "0.40.12"
    val collectionsCompat = "2.9.0"
  }

  lazy val catsCore            = "org.typelevel"          %% "cats-core"                  % Versions.cats
  lazy val catsEffect          = "org.typelevel"          %% "cats-effect"                % Versions.catsEffect
  lazy val catsEffectKernel    = "org.typelevel"          %% "cats-effect-kernel"         % Versions.catsEffect
  lazy val catsFree            = "org.typelevel"          %% "cats-free"                  % Versions.cats
  lazy val catsKernel          = "org.typelevel"          %% "cats-kernel"                % Versions.cats
  lazy val fs2Core             = "co.fs2"                 %% "fs2-core"                   % Versions.fs2
  lazy val fs2Kafka            = "com.github.fd4s"        %% "fs2-kafka"                  % Versions.fs2Kafka
  lazy val kafkaClients        = "org.apache.kafka"        % "kafka-clients"              % Versions.kafkaClients
  lazy val munit               = "org.scalameta"          %% "munit"                      % Versions.munit
  lazy val munitCatsEffect     = "org.typelevel"          %% "munit-cats-effect-3"        % Versions.munitCatsEffect
  lazy val munitScalaCheck     = "org.scalameta"          %% "munit-scalacheck"           % Versions.munit
  lazy val slf4jApi            = "org.slf4j"               % "slf4j-api"                  % Versions.slf4j
  lazy val testcontainersKafka = "com.dimafeng"           %% "testcontainers-scala-kafka" % Versions.testcontainers
  lazy val testcontainersMunit = "com.dimafeng"           %% "testcontainers-scala-munit" % Versions.testcontainers
  lazy val collectionsCompat   = "org.scala-lang.modules" %% "scala-collection-compat"    % Versions.collectionsCompat
  lazy val coreDeps = Seq(
    catsCore,
    catsEffect,
    fs2Core,
    fs2Kafka,
    kafkaClients,
    munit,
    collectionsCompat
  )

  lazy val testDeps = Seq(
    munitCatsEffect,
    munitScalaCheck,
    testcontainersKafka,
    testcontainersMunit
  ).map(_ % "test,it")
}
