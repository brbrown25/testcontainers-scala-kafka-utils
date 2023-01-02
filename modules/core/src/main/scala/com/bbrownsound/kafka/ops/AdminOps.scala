package com.bbrownsound.kafka.ops

import cats.effect._
import fs2.kafka._
import org.apache.kafka.clients.admin.NewTopic

import scala.jdk.CollectionConverters._

import com.bbrownsound.kafka.types._

trait UnsafeAdminOps extends AdminOps {
  def unsafeCreateCustomTopics(
    topics: List[String],
    topicConfig: Map[String, String] = Map.empty,
    partitions: Int = 1,
    replicationFactor: Int = 1
  )(implicit config: C, runtime: unsafe.IORuntime): Unit =
    createCustomTopics[IO](topics, topicConfig, partitions, replicationFactor).unsafeRunSync()

  def unsafeCreateCustomTopic(
    topic: String,
    topicConfig: Map[String, String] = Map.empty,
    partitions: Int = 1,
    replicationFactor: Int = 1
  )(implicit config: C, runtime: unsafe.IORuntime): Unit =
    createCustomTopic[IO](topic, topicConfig, partitions, replicationFactor).unsafeRunSync()

  def unsafeDeleteTopic(topic: String)(implicit config: C, runtime: unsafe.IORuntime): Unit =
    deleteTopic[IO](topic).unsafeRunSync()

  def unsafeDeleteTopics(topics: List[String])(implicit config: C, runtime: unsafe.IORuntime): Unit =
    deleteTopics[IO](topics).unsafeRunSync()
}

trait AdminOps {
  def createCustomTopics[F[_]: Async](
    topics: List[String],
    topicConfig: Map[String, String] = Map.empty,
    partitions: Int = 1,
    replicationFactor: Int = 1
  )(implicit config: C): F[Unit] =
    withAdminClient { (adminClient: KafkaAdminClient[F]) =>
      val newTopics = topics.map(new NewTopic(_, partitions, replicationFactor.toShort).configs(topicConfig.asJava))
      adminClient.createTopics(newTopics)
    }

  def createCustomTopic[F[_]: Async](
    topic: String,
    topicConfig: Map[String, String] = Map.empty,
    partitions: Int = 1,
    replicationFactor: Int = 1
  )(implicit config: C): F[Unit] =
    withAdminClient { (adminClient: KafkaAdminClient[F]) =>
      val newTopic = new NewTopic(topic, partitions, replicationFactor.toShort).configs(topicConfig.asJava)
      adminClient.createTopic(newTopic)
    }

  def deleteTopic[F[_]: Async](topic: String)(implicit config: C): F[Unit] =
    withAdminClient((adminClient: KafkaAdminClient[F]) => adminClient.deleteTopic(topic))

  def deleteTopics[F[_]: Async](topics: List[String])(implicit config: C): F[Unit] =
    withAdminClient((adminClient: KafkaAdminClient[F]) => adminClient.deleteTopics(topics))

  protected def withAdminClient[F[_]: Async, T](
    body: KafkaAdminClient[F] => T
  )(implicit config: C): F[T] =
    KafkaAdminClient.stream[F](AdminClientSettings(config.bootstrapServer)).map(body).compile.lastOrError
}
