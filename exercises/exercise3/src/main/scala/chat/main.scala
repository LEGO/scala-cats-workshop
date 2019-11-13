package chat

import cats.effect._
import cats.effect.concurrent.Ref
import cats.implicits._
import chat.ChatPlugin.{PersonalChatPlugin, PublicChatPlugin}
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

  val publicChatPlugins: List[PublicChatPlugin[IO]]     = List(Plugins.emote)
  val personalChatPlugins: List[PersonalChatPlugin[IO]] = List(Plugins.highlightUser)

  val plugins = Plugins(publicChatPlugins, personalChatPlugins)

  val stream: Stream[IO, Unit] =
    for {
      redisUri    <- Stream.eval(connection.RedisURI.make[IO]("redis://localhost"))
      client      <- Stream.resource(connection.RedisClient[IO](redisUri))
      setCommands <- makeSetCommands(client, redisUri)
      pubsub      <- PubSub.mkPubSubConnection[IO, String, PubSubMessage](client, PubSubCodec, redisUri)
      blocker     <- Stream.resource(Blocker[IO])
      localUsers  <- Stream.eval(Ref.of[IO, Set[String]](Set.empty))
      channel     <- Stream.resource(ChatChannel.makeResource[IO](chatChannel, localUsers, plugins, setCommands, pubsub))
      httpServer = new HttpServer[IO](blocker, channel)
      _ <- httpServer.server.serve
    } yield ()

  def makeSetCommands(client: RedisClient, uri: RedisURI): Stream[IO, SetCommands[IO, String, String]] =
    Stream.resource(Redis[IO, String, String](client, stringCodec, uri))

}

case class Plugins[F[_]](public: PublicChatPlugin[F], personal: PersonalChatPlugin[F]) {
  def publicPipe(username: String): Pipe[F, IncomingWebsocketMessage, IncomingWebsocketMessage] =
    public(username)

  def personalPipe(username: String): Pipe[F, OutgoingWebsocketMessage, OutgoingWebsocketMessage] =
    personal(username)
}

object Plugins {
  def apply[F[_]](
      public: List[PublicChatPlugin[F]],
      personal: List[PersonalChatPlugin[F]]
  ): Plugins[F] =
    Plugins(ChatPlugin.makePipe(public), ChatPlugin.makePipe(personal))

  def emote[F[_]]: PublicChatPlugin[F] = ChatPlugin.publicSync { (username, message) =>
    if (message.text.startsWith("/me "))
      message.copy(text = s"$username ${message.text.stripPrefix("/me ")}", isEmote = Some(true))
    else message
  }

  def highlightUser[F[_]]: PersonalChatPlugin[F] = ChatPlugin.personalSync { (currentUser, message) =>
    message match {
      case msg @ Message(_, _, text, _, _) if text.contains(currentUser) =>
        msg.copy(text = msg.text.replace(currentUser, s"*$currentUser*"))
      case msg => msg
    }
  }
}
