package fs2kafka.example

import cats.effect._
import cats.implicits._
import fs2.kafka.{AutoOffsetReset, ConsumerSettings, ProducerSettings}

import scala.concurrent.duration._

/**
 * @author Kevin Lee
 * @since 2020-02-20
 */
object MainApp extends IOApp {

  val topic: String = "test-topic"

  val consumerSettings =
    ConsumerSettings[IO, String, String]
      .withAutoOffsetReset(AutoOffsetReset.Earliest)
      .withBootstrapServers("localhost:9092")
      .withGroupId("my-group")

  val producerSettings: ProducerSettings[IO, String, String] =
    ProducerSettings[IO, String, String]
      .withBootstrapServers("localhost:9092")


  override def run(args: List[String]): IO[ExitCode] =
    Consumer.consumer[IO, String, String, String, String](
      consumerSettings,
      Topic(topic),
      Topic("producer-topic"),
      MaxConcurrent(5),
      OffsetBatchSize(2),
      TimeWindow(2.seconds),
      producerSettings.some
    )(
      RecordProcessor[IO, String, String, String, String](
        "some-record", record => IO.pure(record.key -> record.value)
      )
    )
    .compile
    .drain
    .as(ExitCode.Success)
}
