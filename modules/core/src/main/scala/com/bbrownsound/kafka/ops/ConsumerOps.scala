package com.bbrownsound.kafka.ops

import cats._
import cats.effect._
import cats.implicits._
import com.bbrownsound.kafka.types._
import fs2.Chunk
import fs2.kafka._
import org.apache.kafka.clients.consumer.ConsumerConfig
import scala.concurrent.duration.DurationInt
import scala.concurrent.duration.FiniteDuration

trait UnsafeConsumerOps extends ConsumerOps {
  def unsafeConsumeFirstStringMessageFrom(
      topic: String,
      autoCommit: Boolean = false,
      timeout: FiniteDuration = 5.seconds
  )(implicit config: C, R: Reducible[Set], runtime: unsafe.IORuntime): String =
    consumeNumberStringMessagesFrom[IO](topic, 1, autoCommit, timeout).map(_.head).unsafeRunSync()

  def unsafeConsumeNumberStringMessagesFrom(
      topic: String,
      number: Int,
      autoCommit: Boolean = false,
      timeout: FiniteDuration = 5.seconds
  )(implicit config: C, R: Reducible[Set], runtime: unsafe.IORuntime): List[String] =
    consumeNumberMessagesFrom[IO, String](topic, number, autoCommit, timeout).unsafeRunSync()

  def unsafeConsumeFirstMessageFrom[V](
      topic: String,
      autoCommit: Boolean = false,
      timeout: FiniteDuration = 5.seconds
  )(
      implicit config: C,
      valueDeserializer: Deserializer[IO, V],
      R: Reducible[Set],
      runtime: unsafe.IORuntime
  ): V =
    consumeNumberMessagesFrom[IO, V](topic, 1, autoCommit, timeout).map(_.head).unsafeRunSync()

  def unsafeConsumeFirstKeyedMessageFrom[K, V](
      topic: String,
      autoCommit: Boolean = false,
      timeout: FiniteDuration = 5.seconds
  )(
      implicit config: C,
      keyDeserializer: Deserializer[IO, K],
      valueDeserializer: Deserializer[IO, V],
      R: Reducible[Set],
      runtime: unsafe.IORuntime
  ): (K, V) = consumeNumberKeyedMessagesFrom[IO, K, V](topic, 1, autoCommit, timeout).map(_.head).unsafeRunSync()

  def unsafeConsumeNumberMessagesFrom[V](
      topic: String,
      number: Int,
      autoCommit: Boolean = false,
      timeout: FiniteDuration = 5.seconds
  )(
      implicit config: C,
      valueDeserializer: Deserializer[IO, V],
      R: Reducible[Set],
      runtime: unsafe.IORuntime
  ): List[V] =
    consumeNumberMessagesFromTopics[IO, V](Set(topic), number, autoCommit, timeout)
      .map(_.values.toList.flatten)
      .unsafeRunSync()

  def unsafeConsumeNumberKeyedMessagesFrom[K, V](
      topic: String,
      number: Int,
      autoCommit: Boolean = false,
      timeout: FiniteDuration = 5.seconds
  )(
      implicit config: C,
      keyDeserializer: Deserializer[IO, K],
      valueDeserializer: Deserializer[IO, V],
      R: Reducible[Set],
      runtime: unsafe.IORuntime
  ): List[(K, V)] = {
    consumeNumberKeyedMessagesFromTopics[IO, K, V](
      Set(topic),
      number,
      autoCommit,
      timeout
    ).map(_.values.toList.flatten).unsafeRunSync()
  }

  def unsafeConsumeNumberMessagesFromTopics[V](
      topics: Set[String],
      number: Int,
      autoCommit: Boolean = false,
      timeout: FiniteDuration = 5.seconds
  )(
      implicit config: C,
      valueDeserializer: Deserializer[IO, V],
      R: Reducible[Set],
      runtime: unsafe.IORuntime
  ): Map[String, List[V]] =
    consumeNumberKeyedMessagesFromTopics[IO, String, V](
      topics,
      number,
      autoCommit,
      timeout
    ).map(
        _.view
          .mapValues(_.map { case (_, m) => m })
          .toMap
      )
      .unsafeRunSync()

  def unsafeConsumeNumberKeyedMessagesFromTopics[K, V](
      topics: Set[String],
      number: Int,
      autoCommit: Boolean = false,
      timeout: FiniteDuration = 5.seconds
  )(
      implicit config: C,
      keyDeserializer: Deserializer[IO, K],
      valueDeserializer: Deserializer[IO, V],
      R: Reducible[Set],
      runtime: unsafe.IORuntime
  ): Map[String, List[(K, V)]] = {
    val consumerProperties = config.customConsumerProperties ++ Map[String, String](
      ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG -> autoCommit.toString
    )
    val settings = ConsumerSettings(
      keyDeserializer = keyDeserializer,
      valueDeserializer = valueDeserializer
    ).withBootstrapServers(config.bootstrapServer).withProperties(consumerProperties)
    KafkaConsumer
      .stream(settings)
      .subscribe(topics)
      .records
      .take(number)
      .map { committable =>
        Map(
          committable.record.topic -> List((committable.record.key, committable.record.value))
        )
      }
      .compile
      .lastOrError
      .unsafeRunSync()
  }

  def unsafeWithConsumer[K, V, T](body: KafkaConsumer[IO, K, V] => T)(
      implicit config: C,
      keyDeserializer: Deserializer[IO, K],
      valueDeserializer: Deserializer[IO, V],
      runtime: unsafe.IORuntime
  ): T = {
    val settings = ConsumerSettings(
      keyDeserializer = keyDeserializer,
      valueDeserializer = valueDeserializer
    ).withBootstrapServers(config.bootstrapServer).withProperties(config.customConsumerProperties)
    KafkaConsumer.stream(settings).map(body).compile.lastOrError.unsafeRunSync()
  }
}

trait ConsumerOps {
  def consumeFirstStringMessageFrom[F[_]: Applicative: Async](
      topic: String,
      autoCommit: Boolean = false,
      timeout: FiniteDuration = 5.seconds
  )(implicit config: C, R: Reducible[Set]): F[String] =
    consumeNumberStringMessagesFrom[F](topic, 1, autoCommit, timeout).map(_.head)

  def consumeNumberStringMessagesFrom[F[_]: Applicative: Async](
      topic: String,
      number: Int,
      autoCommit: Boolean = false,
      timeout: FiniteDuration = 5.seconds
  )(implicit config: C, R: Reducible[Set]): F[List[String]] =
    consumeNumberMessagesFrom[F, String](topic, number, autoCommit, timeout)

  def consumeFirstMessageFrom[F[_]: Applicative: Async, V](
      topic: String,
      autoCommit: Boolean = false,
      timeout: FiniteDuration = 5.seconds
  )(
      implicit config: C,
      valueDeserializer: Deserializer[F, V],
      R: Reducible[Set]
  ): F[V] =
    consumeNumberMessagesFrom[F, V](topic, 1, autoCommit, timeout).map(_.head)

  def consumeFirstKeyedMessageFrom[F[_]: Applicative: Async, K, V](
      topic: String,
      autoCommit: Boolean = false,
      timeout: FiniteDuration = 5.seconds
  )(
      implicit config: C,
      keyDeserializer: Deserializer[F, K],
      valueDeserializer: Deserializer[F, V],
      R: Reducible[Set]
  ): F[(K, V)] = consumeNumberKeyedMessagesFrom[F, K, V](topic, 1, autoCommit, timeout).map(_.head)

  def consumeNumberMessagesFrom[F[_]: Applicative: Async, V](
      topic: String,
      number: Int,
      autoCommit: Boolean = false,
      timeout: FiniteDuration = 5.seconds
  )(
      implicit config: C,
      valueDeserializer: Deserializer[F, V],
      R: Reducible[Set]
  ): F[List[V]] =
    consumeNumberMessagesFromTopics[F, V](Set(topic), number, autoCommit, timeout).map(_.values.toList.flatten)

  def consumeNumberKeyedMessagesFrom[F[_]: Applicative: Async, K, V](
      topic: String,
      number: Int,
      autoCommit: Boolean = false,
      timeout: FiniteDuration = 5.seconds
  )(
      implicit config: C,
      keyDeserializer: Deserializer[F, K],
      valueDeserializer: Deserializer[F, V],
      R: Reducible[Set]
  ): F[List[(K, V)]] = {
    consumeNumberKeyedMessagesFromTopics[F, K, V](
      Set(topic),
      number,
      autoCommit,
      timeout
    ).map(_.values.toList.flatten)
  }

  def consumeNumberMessagesFromTopics[F[_]: Applicative: Async, V](
      topics: Set[String],
      number: Int,
      autoCommit: Boolean = false,
      timeout: FiniteDuration = 5.seconds
  )(
      implicit config: C,
      valueDeserializer: Deserializer[F, V],
      R: Reducible[Set]
  ): F[Map[String, List[V]]] =
    consumeNumberKeyedMessagesFromTopics[F, Unit, V](
      topics,
      number,
      autoCommit,
      timeout
    ).map(
      _.view
        .mapValues(_.map { case (_, m) => m })
        .toMap
    )

  def consumeNumberKeyedMessagesFromTopics[F[_]: Applicative: Async, K, V](
      topics: Set[String],
      number: Int,
      autoCommit: Boolean = false,
      timeout: FiniteDuration = 5.seconds
  )(
      implicit config: C,
      keyDeserializer: Deserializer[F, K],
      valueDeserializer: Deserializer[F, V],
      R: Reducible[Set]
  ): F[Map[String, List[(K, V)]]] = {
    val consumerProperties = config.customConsumerProperties ++ Map[String, String](
      ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG -> autoCommit.toString
    )
    val settings = ConsumerSettings(
      keyDeserializer = keyDeserializer,
      valueDeserializer = valueDeserializer
    ).withBootstrapServers(config.bootstrapServer).withProperties(consumerProperties)
    KafkaConsumer
      .stream(settings)
      .subscribe(topics)
      .records
      .take(number)
      .map { committable =>
        (
          Map(
            committable.record.topic -> List((committable.record.key, committable.record.value))
          ),
          committable.offset
        )
      }
      .through(handleCommit(autoCommit, _))
      .unchunks
      .compile
      .lastOrError
  }

  private def handleCommit[F[_]: Applicative: Async, V, K](
      autoCommit: Boolean,
      c: fs2.Stream[F, (Map[String, List[(K, V)]], CommittableOffset[F])]
  ): fs2.Stream[F, Chunk[Map[String, List[(K, V)]]]] = {
    if (autoCommit) {
      c.groupWithin(500, 15.seconds).evalMap { s =>
        CommittableOffsetBatch.fromFoldable(s.map(_._2)).commit.map(_ => s.map(_._1))
      }
    } else {
      c.map(r => Chunk(r._1))
    }
  }

  def withConsumer[F[_]: Applicative: Async, K, V, T](body: KafkaConsumer[F, K, V] => T)(
      implicit config: C,
      keyDeserializer: Deserializer[F, K],
      valueDeserializer: Deserializer[F, V]
  ): F[T] = {
    val settings = ConsumerSettings(
      keyDeserializer = keyDeserializer,
      valueDeserializer = valueDeserializer
    ).withBootstrapServers(config.bootstrapServer).withProperties(config.customConsumerProperties)
    KafkaConsumer.stream(settings).map(body).compile.lastOrError
  }
}
