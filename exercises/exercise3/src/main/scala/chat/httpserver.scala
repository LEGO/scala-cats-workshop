package chat

import cats.effect._
import cats.implicits._
import fs2.Pipe
import io.circe.parser._
import io.circe.syntax._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.staticcontent._
import org.http4s.server.websocket._
import org.http4s.syntax.all._
import org.http4s.websocket.WebSocketFrame
import org.http4s.websocket.WebSocketFrame.Text
import org.http4s.{HttpRoutes, Response, StaticFile}

class HttpServer(blocker: Blocker, channel: Channel)(implicit cs: ContextShift[IO], tmr: Timer[IO])
    extends Http4sDsl[IO] {
  object UsernameParam extends QueryParamDecoderMatcher[String]("username")

  val chatRoutes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "chat" :? UsernameParam(username) =>
      channel
        .connect(username)
        .flatMap(buildWebSocket)
    case req @ GET -> Root =>
      StaticFile.fromResource("/frontend/index.html", blocker, req.some).getOrElseF(NotFound())
  }

  val staticRoutes: HttpRoutes[IO] =
    resourceService[IO](
      ResourceService.Config(
        basePath = "/frontend",
        blocker = blocker
      )
    )

  val routes: HttpRoutes[IO] = chatRoutes <+> staticRoutes

  val server: BlazeServerBuilder[IO] =
    BlazeServerBuilder[IO]
      .bindLocal(4000)
      .withHttpApp(routes.orNotFound)

  def buildWebSocket(session: Session): IO[Response[IO]] =
    WebSocketBuilder[IO].build(
      // Encode pubsub messages to websocket frames and make sure we disconnect the client when the websocket connection ends
      session.toClient.through(encodeWebsocketFrame).onFinalize(channel.disconnect(session)),
      // Attempt decoding to pubsubmessage, and drop frames that fail to decode
      _.through(decodeWebsocketFrame).through(session.fromClient)
    )

  def encodeWebsocketFrame: Pipe[IO, OutgoingWebsocketMessage, WebSocketFrame] =
    _.map(_.asJson.spaces2SortKeys).map(Text(_))
  def decodeWebsocketFrame: Pipe[IO, WebSocketFrame, IncomingWebsocketMessage] =
    _.collect {
      case Text(msg, _) => decode[IncomingWebsocketMessage](msg).toOption
    }.unNone
}
