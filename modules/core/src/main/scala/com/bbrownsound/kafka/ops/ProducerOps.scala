package com.bbrownsound.kafka.ops

import cats.effect._
import cats.implicits._
import fs2.kafka.KafkaProducer
import fs2.kafka.ProducerRecord
import fs2.kafka.ProducerRecords
import fs2.kafka.ProducerSettings
import fs2.kafka.Serializer

import com.bbrownsound.kafka.types._

/** Wraps the producer ops an runs them using the provided IO runtime */
trait UnsafeProducerOps extends ProducerOps {

  /**
   * Produce a String Message to a topic with an empty key
   * @param topic
   * @param message
   * @param config
   * @param runtime
   */
  def unsafePublishStringMessageToKafka(
    topic: String,
    message: String
  )(implicit config: C, runtime: unsafe.IORuntime): Unit =
    publishStringMessageToKafka[IO](topic, message).unsafeRunSync()

  /**
   * Produce a keyed message to the provided topic
   * @param topic
   * @param key
   * @param message
   * @param config
   * @param keySerializer
   * @param serializer
   * @param runtime
   * @tparam K
   * @tparam V
   */
  def unsafePublishToKafka[K, V](topic: String, key: K, message: V)(
    implicit config: C,
    keySerializer: Serializer[IO, K],
    serializer: Serializer[IO, V],
    runtime: unsafe.IORuntime
  ): Unit = publishToKafka[IO, K, V](topic, key, message).unsafeRunSync()

  /**
   * Produce multiple messages to the provided topic
   * @param topic
   * @param messages
   * @param config
   * @param keySerializer
   * @param serializer
   * @param runtime
   * @tparam K
   * @tparam V
   */
  def unsafePublishToKafka[K, V](topic: String, messages: Seq[(K, V)])(
    implicit config: C,
    keySerializer: Serializer[IO, K],
    serializer: Serializer[IO, V],
    runtime: unsafe.IORuntime
  ): Unit = publishToKafka[IO, K, V](topic, messages).unsafeRunSync()

  /**
   * Run the provided action with the producer
   * @param body
   * @param config
   * @param keySerializer
   * @param valueSerializer
   * @param runtime
   * @tparam K
   * @tparam V
   * @tparam T
   * @return
   */
  def unsafeWithProducer[K, V, T](body: KafkaProducer.Metrics[IO, K, V] => T)(
    implicit config: C,
    keySerializer: Serializer[IO, K],
    valueSerializer: Serializer[IO, V],
    runtime: unsafe.IORuntime
  ): T = withProducer[IO, K, V, T](body).unsafeRunSync()
}

trait ProducerOps {

  /**
   * Produces a single string message to the provided topic
   * @param topic
   * @param message
   * @param config
   * @tparam F
   * @return
   */
  def publishStringMessageToKafka[F[_]: Async](topic: String, message: String)(implicit config: C): F[Unit] =
    publishToKafka[F, String, String](topic, "", message)

  /**
   * Produce a keyed message to the specified topic
   * @param topic
   * @param key
   * @param message
   * @param config
   * @param keySerializer
   * @param serializer
   * @tparam F
   * @tparam K
   * @tparam V
   * @return
   */
  def publishToKafka[F[_]: Async, K, V](topic: String, key: K, message: V)(
    implicit config: C,
    keySerializer: Serializer[F, K],
    serializer: Serializer[F, V]
  ): F[Unit] =
    publishToKafka(topic, Seq((key, message)))

  /**
   * Given a producer record, produce to a topic
   * @param producerRecord
   * @param config
   * @param serializer
   * @tparam F
   * @tparam V
   * @return
   */
  def publishToKafka[F[_]: Async, V](
    producerRecord: ProducerRecord[String, V]
  )(implicit config: C, serializer: Serializer[F, V]): F[Unit] = {
    val settings: ProducerSettings[F, String, V] = ProducerSettings(
      keySerializer = Serializer[F, String],
      valueSerializer = Serializer[F, V]
    ).withBootstrapServers(config.bootstrapServer).withProperties(config.customProducerProperties)
    publishToKafka(
      KafkaProducer.stream(settings),
      ProducerRecords.one(producerRecord)
    )
  }

  /**
   * Produce a collection of messages to a topic
   * @param topic
   * @param messages
   * @param config
   * @param keySerializer
   * @param serializer
   * @tparam F
   * @tparam K
   * @tparam V
   * @return
   */
  def publishToKafka[F[_]: Async, K, V](topic: String, messages: Seq[(K, V)])(
    implicit config: C,
    keySerializer: Serializer[F, K],
    serializer: Serializer[F, V]
  ): F[Unit] = {
    val settings: ProducerSettings[F, K, V] = ProducerSettings(
      keySerializer = Serializer[F, K],
      valueSerializer = Serializer[F, V]
    ).withBootstrapServers(config.bootstrapServer).withProperties(config.customProducerProperties)
    val records: Seq[ProducerRecord[K, V]] = messages.map { msg =>
      val (k, v) = msg
      ProducerRecord(topic, k, v)
    }
    publishToKafka(
      KafkaProducer.stream(settings),
      ProducerRecords(records.toList)
    )
  }

  /**
   * Given a body do some work with a producer and return a result
   * @param body
   * @param config
   * @param keySerializer
   * @param valueSerializer
   * @tparam F
   * @tparam K
   * @tparam V
   * @tparam T
   * @return
   */
  def withProducer[F[_]: Async, K, V, T](body: KafkaProducer.Metrics[F, K, V] => T)(
    implicit config: C,
    keySerializer: Serializer[F, K],
    valueSerializer: Serializer[F, V]
  ): F[T] = {
    val settings = ProducerSettings(
      keySerializer = keySerializer,
      valueSerializer = valueSerializer
    ).withBootstrapServers(config.bootstrapServer).withProperties(config.customProducerProperties)
    KafkaProducer.stream(settings).map(body).compile.lastOrError
  }

  /**
   * Publish the provided records to a topic
   * @param kafkaProducer
   * @param records
   * @tparam F
   * @tparam K
   * @tparam V
   * @return
   */
  private def publishToKafka[F[_]: Async, K, V](
    kafkaProducer: fs2.Stream[F, KafkaProducer.Metrics[F, K, V]],
    records: ProducerRecords[Unit, K, V]
  ): F[Unit] =
    kafkaProducer
      .evalMap(producer => producer.produce(records).flatten)
      .compile
      .drain
}
