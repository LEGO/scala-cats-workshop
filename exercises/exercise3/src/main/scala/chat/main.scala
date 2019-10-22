package chat

import cats.effect._
import cats.implicits._
import dev.profunktor.redis4cats.algebra.SetCommands
import dev.profunktor.redis4cats.connection
import dev.profunktor.redis4cats.domain._
import dev.profunktor.redis4cats.interpreter.pubsub.PubSub
import dev.profunktor.redis4cats.interpreter.Redis
import dev.profunktor.redis4cats.log4cats._
import fs2.Stream
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import io.lettuce.core.RedisURI

object Main extends IOApp {
  implicit val logger: Logger[IO] = Slf4jLogger.getLogger

  val stringCodec = RedisCodec.Utf8
  val chatChannel = LiveChannel("chat")

  def run(args: List[String]): IO[ExitCode] =
    stream.compile.drain.as(ExitCode.Success)

  val stream = for {
    redisUri    <- Stream.eval(connection.RedisURI.make[IO]("redis://localhost"))
    client      <- Stream.resource(connection.RedisClient[IO](redisUri))
    setCommands <- makeSetCommands(client, redisUri)
    pubsub      <- PubSub.mkPubSubConnection[IO, String, PubSubMessage](client, PubSubCodec, redisUri)
    channel    = new ChatChannel[IO](chatChannel, setCommands, pubsub)
    httpServer = new HttpServer[IO](channel)
    _ <- httpServer.server.serve
  } yield ()

  def makeSetCommands(client: RedisClient, uri: RedisURI): Stream[IO, SetCommands[IO, String, String]] =
    Stream.resource(Redis[IO, String, String](client, stringCodec, uri))

}
