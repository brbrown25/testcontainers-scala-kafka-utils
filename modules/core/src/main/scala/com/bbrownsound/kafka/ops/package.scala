package com.bbrownsound.kafka

package object ops {
  trait UnsafeOps extends UnsafeProducerOps with UnsafeConsumerOps with UnsafeAdminOps
  trait SafeOps extends ProducerOps with ConsumerOps with AdminOps
}
