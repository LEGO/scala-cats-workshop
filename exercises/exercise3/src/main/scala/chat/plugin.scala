package chat

import cats.effect.IO
import fs2.Pipe

object ChatPlugin {

  type UserName        = String
  type ChatPlugin[Msg] = UserName => Pipe[IO, Msg, Msg]

  // A public chat plugin works on messages coming from the user, before sending them on to the outside world.
  // It can be features like processing commands, that the other servers might not have implemented.
  type PublicChatPlugin = ChatPlugin[IncomingWebsocketMessage]

  // A personal chat plugin works on messages coming from the outside, before being sent to the user.
  // It can be stuff like highlighting your username in messages.
  type PersonalChatPlugin = ChatPlugin[OutgoingWebsocketMessage]

  def public(
      transform: (UserName, IncomingWebsocketMessage) => IO[IncomingWebsocketMessage]
  ): PublicChatPlugin =
    userName => _.evalMap(msg => transform(userName, msg))

  def publicSync(
      transform: (UserName, IncomingWebsocketMessage) => IncomingWebsocketMessage
  ): PublicChatPlugin =
    userName => _.map(msg => transform(userName, msg))

  def personal(
      transform: (UserName, OutgoingWebsocketMessage) => IO[OutgoingWebsocketMessage]
  ): PersonalChatPlugin =
    userName => _.evalMap(msg => transform(userName, msg))

  def personalSync(
      transform: (UserName, OutgoingWebsocketMessage) => OutgoingWebsocketMessage
  ): PersonalChatPlugin =
    userName => _.map(msg => transform(userName, msg))

  def makePipe[Msg](
      plugins: List[ChatPlugin[Msg]]
  ): UserName => Pipe[IO, Msg, Msg] =
    userName =>
      plugins
        .map(p => p(userName))
        .foldLeft[Pipe[IO, Msg, Msg]](identity)(_.andThen(_))
}
