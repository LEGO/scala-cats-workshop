package chat

import cats.effect._
import cats.implicits._
import fs2.Pipe
import io.circe.parser._
import io.circe.syntax._
import org.http4s.dsl.Http4sDsl
import org.http4s.{HttpRoutes, Response}
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.websocket._
import org.http4s.syntax.all._
import org.http4s.websocket.WebSocketFrame
import org.http4s.websocket.WebSocketFrame.Text

class HttpServer[F[_]: ConcurrentEffect: Timer](channel: Channel[F]) extends Http4sDsl[F] {
  object UsernameParam extends QueryParamDecoderMatcher[String]("username")

  val routes = HttpRoutes.of[F] {
    case GET -> Root / "chat" :? UsernameParam(username) =>
      channel
        .connect(username)
        .flatMap(buildWebSocket)
  }

  val server = BlazeServerBuilder[F]
    .bindLocal(4000)
    .withHttpApp(routes.orNotFound)

  def buildWebSocket(session: Session[F]): F[Response[F]] =
    WebSocketBuilder[F].build(
      // Encode pubsub messages to websocket frames and make sure we disconnect the client when the websocket connection ends
      session.toClient.through(encodeWebsocketFrame).onFinalize(channel.disconnect(session)),
      // Attempt decoding to pubsubmessage, and drop frames that fail to decode
      _.through(decodeWebsocketFrame).to(session.fromClient)
    )

  def encodeWebsocketFrame: Pipe[F, OutgoingWebsocketMessage, WebSocketFrame] =
    _.map(_.asJson.spaces2SortKeys).map(Text(_))
  def decodeWebsocketFrame: Pipe[F, WebSocketFrame, IncomingWebsocketMessage] =
    _.collect {
      case Text(msg, _) => decode[IncomingWebsocketMessage](msg).toOption
    }.unNone
}
