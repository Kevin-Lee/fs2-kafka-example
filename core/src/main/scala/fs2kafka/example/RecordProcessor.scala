package fs2kafka.example

import cats.Applicative
import fs2.kafka.ConsumerRecord

/**
 * @author Kevin Lee
 * @since 2020-02-20
 */
case class RecordProcessor[F[_] : Applicative, K, V, KK, VV](
  name: String,
  f: ConsumerRecord[K, V] => F[(KK, VV)]
) extends (ConsumerRecord[K, V] => F[(KK, VV)]) {
  def apply(record: ConsumerRecord[K, V]): F[(KK, VV)] = f(record)
}
