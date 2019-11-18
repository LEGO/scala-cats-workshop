package chat.plugins

import cats.effect.IO
import chat.{IncomingWebsocketMessage, OutgoingWebsocketMessage}
import fs2.Pipe
import cats.implicits._

import scala.util.Try

object ChatPlugin {

  type UserName        = String
  type ChatPlugin[Msg] = UserName => Pipe[IO, Msg, Msg]

  // A public chat plugin works on messages coming from the user, before sending them on to the outside world.
  // It can be features like processing commands, that the other servers might not have implemented.
  type PublicChatPlugin = ChatPlugin[IncomingWebsocketMessage]

  // A personal chat plugin works on messages coming from the outside, before being sent to the user.
  // It can be stuff like highlighting your username in messages.
  type PersonalChatPlugin = ChatPlugin[OutgoingWebsocketMessage]

  val incomingRecovery: PartialFunction[Throwable, IncomingWebsocketMessage] = {
    case err => IncomingWebsocketMessage(err.getMessage, None, Option(true))
  }
  val outgoingRecovery: PartialFunction[Throwable, OutgoingWebsocketMessage] = {
    case err => OutgoingWebsocketMessage.Message(System.currentTimeMillis(), "SYSTEM ", err.getMessage, true, None)
  }

  def public(
      transform: (UserName, IncomingWebsocketMessage) => IO[IncomingWebsocketMessage]
  ): PublicChatPlugin =
    userName =>
      _.evalMap(
        msg => transform(userName, msg).recover(incomingRecovery)
      )

  def publicSync(
      transform: (UserName, IncomingWebsocketMessage) => IncomingWebsocketMessage
  ): PublicChatPlugin =
    userName =>
      _.map(
        msg =>
          Either
            .catchNonFatal(transform(userName, msg))
            .leftMap(incomingRecovery)
            .merge
      )

  def personal(
      transform: (UserName, OutgoingWebsocketMessage) => IO[OutgoingWebsocketMessage]
  ): PersonalChatPlugin =
    userName => _.evalMap(msg => transform(userName, msg).recover(outgoingRecovery))

  def personalSync(
      transform: (UserName, OutgoingWebsocketMessage) => OutgoingWebsocketMessage
  ): PersonalChatPlugin =
    userName => _.map(msg => Either.catchNonFatal(transform(userName, msg)).leftMap(outgoingRecovery).merge)

  def makePipe[Msg](
      plugins: List[ChatPlugin[Msg]]
  ): UserName => Pipe[IO, Msg, Msg] =
    userName =>
      plugins
        .mapApply(userName)
        .foldLeft[Pipe[IO, Msg, Msg]](identity)(_.andThen(_))
}
