package fs2kafka.example

import scala.concurrent.duration.FiniteDuration

/**
 * @author Kevin Lee
 * @since 2020-02-20
 */
final case class Topic(topic: String) extends AnyVal
final case class MaxConcurrent(maxConcurrent: Int) extends AnyVal
final case class OffsetBatchSize(offsetBatchSize: Int) extends AnyVal
final case class TimeWindow(timeWindow: FiniteDuration) extends AnyVal


