package com.bbrownsound.kafka

package object ops {
  trait UnsafeOps extends UnsafeProducerOps with UnsafeAdminOps
  trait SafeOps extends ProducerOps with AdminOps
}
