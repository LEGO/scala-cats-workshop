package chat

import cats.effect._
import cats.implicits._
import chat.ChatPlugin.{IncomingChatPlugin, OutgoingChatPlugin}
import chat.OutgoingWebsocketMessage.Message
import dev.profunktor.redis4cats.algebra.SetCommands
import dev.profunktor.redis4cats.connection
import dev.profunktor.redis4cats.domain._
import dev.profunktor.redis4cats.interpreter.Redis
import dev.profunktor.redis4cats.interpreter.pubsub.PubSub
import dev.profunktor.redis4cats.log4cats._
import fs2.{Pipe, Stream}
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import io.lettuce.core.RedisURI

object Main extends IOApp {
  implicit val logger: Logger[IO] = Slf4jLogger.getLogger

  val stringCodec: LiveRedisCodec[String, String] = RedisCodec.Utf8
  val chatChannel: LiveChannel[String]            = LiveChannel("chat")

  def run(args: List[String]): IO[ExitCode] =
    stream.compile.drain.as(ExitCode.Success)

  val incomingPlugins: List[IncomingChatPlugin[IO]] = List(Plugins.emote)
  val outgoingPlugins: List[OutgoingChatPlugin[IO]] = List(Plugins.highlightUser)

  val plugins = Plugins(incomingPlugins, outgoingPlugins)

  val stream: Stream[IO, Unit] =
    for {
      redisUri    <- Stream.eval(connection.RedisURI.make[IO]("redis://localhost"))
      client      <- Stream.resource(connection.RedisClient[IO](redisUri))
      setCommands <- makeSetCommands(client, redisUri)
      pubsub      <- PubSub.mkPubSubConnection[IO, String, PubSubMessage](client, PubSubCodec, redisUri)
      blocker     <- Stream.resource(Blocker[IO])
      channel    = new ChatChannel[IO](chatChannel, plugins, setCommands, pubsub)
      httpServer = new HttpServer[IO](blocker, channel)
      _ <- httpServer.server.serve
    } yield ()

  def makeSetCommands(client: RedisClient, uri: RedisURI): Stream[IO, SetCommands[IO, String, String]] =
    Stream.resource(Redis[IO, String, String](client, stringCodec, uri))

}

case class Plugins[F[_]](incoming: IncomingChatPlugin[F], outgoing: OutgoingChatPlugin[F]) {
  def incomingPipe(username: String): Pipe[F, IncomingWebsocketMessage, IncomingWebsocketMessage] =
    incoming(username)

  def outgoingPipe(username: String): Pipe[F, OutgoingWebsocketMessage, OutgoingWebsocketMessage] =
    outgoing(username)
}

object Plugins {
  def apply[F[_]](
      incomingPlugins: List[IncomingChatPlugin[F]],
      outgoingPlugins: List[OutgoingChatPlugin[F]]
  ): Plugins[F] =
    Plugins(ChatPlugin.makePipe(incomingPlugins), ChatPlugin.makePipe(outgoingPlugins))

  def emote[F[_]]: IncomingChatPlugin[F] = ChatPlugin.incomingSync { (username, message) =>
    if (message.text.startsWith("/me "))
      message.copy(text = s"$username ${message.text.stripPrefix("/me ")}", isEmote = Some(true))
    else message
  }

  def highlightUser[F[_]]: OutgoingChatPlugin[F] = ChatPlugin.outgoingSync { (currentUser, message) =>
    message match {
      case msg @ Message(_, _, text, _, _) if text.contains(currentUser) =>
        msg.copy(text = msg.text.replace(currentUser, s"*$currentUser*"))
      case msg => msg
    }
  }
}
