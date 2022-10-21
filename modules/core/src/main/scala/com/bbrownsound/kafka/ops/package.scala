package com.bbrownsound.kafka

package object ops {
  trait UnsafeOps extends UnsafeProducerOps
  trait SafeOps extends ProducerOps
}
