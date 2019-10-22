package chat

import cats.effect._
import cats.implicits._
import fs2.{Pipe, Stream}
import java.util.concurrent.TimeUnit

trait Session[F[_]] {
  def username: String
  def toClient: Stream[F, OutgoingWebsocketMessage]
  def fromClient: Pipe[F, IncomingWebsocketMessage, Unit]
}

class ChatSession[F[_]: Sync](
    val username: String,
    initialUserList: List[String],
    publish: Pipe[F, PubSubMessage, Unit],
    subscribe: Stream[F, PubSubMessage]
)(implicit timer: Timer[F])
    extends Session[F] {

  val timestamp: F[Long] = timer.clock.realTime(TimeUnit.SECONDS)

  val toClient: Stream[F, OutgoingWebsocketMessage] =
    Stream(OutgoingWebsocketMessage.UserList(initialUserList)) ++
      subscribe
        .evalMap(msg => timestamp.map(ts => OutgoingWebsocketMessage.fromPubSubMessage(msg, ts)))

  val fromClient: Pipe[F, IncomingWebsocketMessage, Unit] =
    _.map(_.toPubSubMessage(username)).to(publish)

}
