package com.bbrownsound.kafka

case class TestContainersKafkaConfig(
    bootstrapServer: String,
    customBrokerProperties: Map[String, String] = Map.empty,
    customProducerProperties: Map[String, String] = Map.empty,
    customConsumerProperties: Map[String, String] = Map.empty
)
