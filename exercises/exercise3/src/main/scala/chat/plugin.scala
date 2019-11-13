package chat

import fs2.Pipe

object ChatPlugin {
  type UserName              = String
  type ChatPlugin[F[_], Msg] = UserName => Pipe[F, Msg, Msg]
  // A public chat plugin works on messages coming from the user, before sending them on to the outside world.
  // It can be features like processing commands, that the other servers might not have implemented.
  type PublicChatPlugin[F[_]] = ChatPlugin[F, IncomingWebsocketMessage]
  // A personal chat plugin works on messages coming from the outside, before being sent to the user.
  // It can be stuff like highlighting your username in messages.
  type PersonalChatPlugin[F[_]] = ChatPlugin[F, OutgoingWebsocketMessage]

  def public[F[_]](
      transform: (UserName, IncomingWebsocketMessage) => F[IncomingWebsocketMessage]
  ): PublicChatPlugin[F] =
    userName => _.evalMap(msg => transform(userName, msg))

  def publicSync[F[_]](
      transform: (UserName, IncomingWebsocketMessage) => IncomingWebsocketMessage
  ): PublicChatPlugin[F] =
    userName => _.map(msg => transform(userName, msg))

  def personal[F[_]](
      transform: (UserName, OutgoingWebsocketMessage) => F[OutgoingWebsocketMessage]
  ): PersonalChatPlugin[F] =
    userName => _.evalMap(msg => transform(userName, msg))

  def personalSync[F[_]](
      transform: (UserName, OutgoingWebsocketMessage) => OutgoingWebsocketMessage
  ): PersonalChatPlugin[F] =
    userName => _.map(msg => transform(userName, msg))

  def makePipe[F[_], Msg](
      plugins: List[ChatPlugin[F, Msg]]
  ): UserName => Pipe[F, Msg, Msg] =
    userName =>
      plugins
        .map(p => p(userName))
        .foldLeft[Pipe[F, Msg, Msg]](identity)(_.andThen(_))
}
