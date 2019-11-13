package chat

import fs2.Pipe

object ChatPlugin {
  type UserName                 = String
  type ChatPlugin[F[_], Msg]    = UserName => Pipe[F, Msg, Msg]
  type IncomingChatPlugin[F[_]] = UserName => Pipe[F, IncomingWebsocketMessage, IncomingWebsocketMessage]
  type OutgoingChatPlugin[F[_]] = UserName => Pipe[F, OutgoingWebsocketMessage, OutgoingWebsocketMessage]

  def incoming[F[_]](
      transform: (UserName, IncomingWebsocketMessage) => F[IncomingWebsocketMessage]
  ): IncomingChatPlugin[F] =
    userName => _.evalMap(msg => transform(userName, msg))

  def incomingSync[F[_]](
      transform: (UserName, IncomingWebsocketMessage) => IncomingWebsocketMessage
  ): IncomingChatPlugin[F] =
    userName => _.map(msg => transform(userName, msg))

  def outgoing[F[_]](
      transform: (UserName, OutgoingWebsocketMessage) => F[OutgoingWebsocketMessage]
  ): OutgoingChatPlugin[F] =
    userName => _.evalMap(msg => transform(userName, msg))

  def outgoingSync[F[_]](
      transform: (UserName, OutgoingWebsocketMessage) => OutgoingWebsocketMessage
  ): OutgoingChatPlugin[F] =
    userName => _.map(msg => transform(userName, msg))

  def makePipe[F[_], Msg](
      plugins: List[ChatPlugin[F, Msg]]
  ): UserName => Pipe[F, Msg, Msg] =
    userName =>
      plugins
        .map(p => p(userName))
        .foldLeft[Pipe[F, Msg, Msg]](identity)(_.andThen(_))
}

