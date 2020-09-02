package fs2kafka.example

import cats._
import cats.effect._
import cats.implicits._

import fs2.Stream
import fs2.kafka._

import scala.{Stream => _}

/**
 * @author Kevin Lee
 * @since 2020-02-20
 */
object Consumer {

  def consumer[F[_] : ConcurrentEffect : ContextShift : Timer : Applicative, K, V, KK, VV](
    consumerSettings: ConsumerSettings[F, K, V],
    topic: Topic,
    producerTopic: Topic,
    maxConcurrent: MaxConcurrent,
    offsetBatchSize: OffsetBatchSize,
    timeWindow: TimeWindow,
    producerConfig: Option[ProducerSettings[F, KK, VV]]
  )(
    recordProcessor: RecordProcessor[F, K, V, KK, VV],
  ): Stream[F, Unit] = {

    val stream = consumerStream[F]
      .using(consumerSettings)
      .evalTap(_.subscribeTo(topic.topic))
      .flatMap(_.stream)
      .mapAsync(maxConcurrent.maxConcurrent) { committable =>
        recordProcessor(committable.record)
          .map { case (k, v) =>
            val record = ProducerRecord(producerTopic.topic, k, v)
            ProducerRecords.one(record, committable.offset)
          }
      }
    producerConfig.fold(
        stream.map(_.passthrough)
      )(producerConfig =>
        stream.through(produce(producerConfig))
          .map(_.passthrough)
      )
      .through(commitBatchWithin(offsetBatchSize.offsetBatchSize, timeWindow.timeWindow))
  }
}
