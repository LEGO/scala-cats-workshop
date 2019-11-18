package chat

import cats.effect._
import cats.implicits._
import fs2.{Pipe, Stream}
import java.util.concurrent.TimeUnit

trait Session {
  def username: String
  def toClient: Stream[IO, OutgoingWebsocketMessage]
  def fromClient: Pipe[IO, IncomingWebsocketMessage, Unit]
}

class ChatSession(
    val username: String,
    initialUserList: List[String],
    incomingPipe: Pipe[IO, IncomingWebsocketMessage, IncomingWebsocketMessage],
    outgoingPipe: Pipe[IO, OutgoingWebsocketMessage, OutgoingWebsocketMessage],
    publish: Pipe[IO, PubSubMessage, Unit],
    subscribe: Stream[IO, PubSubMessage]
)(implicit timer: Timer[IO])
    extends Session {

  val timestamp: IO[Long] = timer.clock.realTime(TimeUnit.SECONDS)

  val toClient: Stream[IO, OutgoingWebsocketMessage] =
    Stream(OutgoingWebsocketMessage.Connected(username, initialUserList)) ++
      subscribe
        .evalMap(msg => timestamp.map(ts => OutgoingWebsocketMessage.fromPubSubMessage(msg, ts)))
        .through(outgoingPipe)

  val fromClient: Pipe[IO, IncomingWebsocketMessage, Unit] =
    _.through(incomingPipe)
      .map(_.toPubSubMessage(username))
      .through(publish)

}
