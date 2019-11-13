package chat

import dev.profunktor.redis4cats.domain.{JCodec, RedisCodec}
import io.circe.Codec
import io.circe.derivation.deriveCodec
import io.circe.parser._
import io.circe.syntax._
import io.lettuce.core.codec.{StringCodec, ToByteBufEncoder, RedisCodec => JRedisCodec}
import io.netty.buffer.ByteBuf
import java.nio.ByteBuffer

case class IncomingWebsocketMessage(text: String, imageUrl: Option[String], isEmote: Option[Boolean]) {
  def toPubSubMessage(username: String): PubSubMessage.Message =
    PubSubMessage.Message(username, text, isEmote.getOrElse(false), imageUrl)
}
object IncomingWebsocketMessage {
  implicit val codec: Codec[IncomingWebsocketMessage] = deriveCodec
}

sealed trait OutgoingWebsocketMessage
object OutgoingWebsocketMessage {
  implicit val messageCodec: Codec.AsObject[Message]       = deriveCodec
  implicit val connectedCodec: Codec.AsObject[Connected]   = deriveCodec
  implicit val userJoinedCodec: Codec.AsObject[UserJoined] = deriveCodec
  implicit val userLeftCodec: Codec.AsObject[UserLeft]     = deriveCodec
  implicit val codec: Codec[OutgoingWebsocketMessage]      = deriveCodec

  def fromPubSubMessage(psm: PubSubMessage, timestamp: Long): OutgoingWebsocketMessage =
    psm match {
      case PubSubMessage.Join(username, userList)  => UserJoined(timestamp, username, userList)
      case PubSubMessage.Leave(username, userList) => UserLeft(timestamp, username, userList)
      case PubSubMessage.Message(username: String, text: String, isEmote: Boolean, imageUrl: Option[String]) =>
        Message(timestamp, username, text, isEmote, imageUrl)
    }

  // "Callback" with the unique username after connection
  case class Connected(username: String, userList: List[String]) extends OutgoingWebsocketMessage
  case class Message(
      timestamp: Long,
      username: String,
      text: String,
      isEmote: Boolean,
      imageUrl: Option[String]
  ) extends OutgoingWebsocketMessage
  case class UserJoined(timestamp: Long, username: String, userList: List[String]) extends OutgoingWebsocketMessage
  case class UserLeft(timestamp: Long, username: String, userList: List[String])   extends OutgoingWebsocketMessage
}

sealed trait PubSubMessage
object PubSubMessage {
  implicit val joinCodec: Codec.AsObject[Join]       = deriveCodec
  implicit val leaveCodec: Codec.AsObject[Leave]     = deriveCodec
  implicit val messageCodec: Codec.AsObject[Message] = deriveCodec
  implicit val codec: Codec[PubSubMessage]           = deriveCodec

  case class Join(username: String, userList: List[String])                                      extends PubSubMessage
  case class Leave(username: String, userList: List[String])                                     extends PubSubMessage
  case class Message(username: String, text: String, isEmote: Boolean, imageUrl: Option[String]) extends PubSubMessage
}

object PubSubCodec extends RedisCodec[String, PubSubMessage] {
  val codec: StringCodec = StringCodec.UTF8
  val underlying: JCodec[String, PubSubMessage] =
    new JRedisCodec[String, PubSubMessage] with ToByteBufEncoder[String, PubSubMessage] {
      override def decodeKey(bytes: ByteBuffer): String = codec.decodeKey(bytes)
      override def encodeKey(key: String): ByteBuffer   = codec.encodeKey(key)

      override def encodeValue(value: PubSubMessage): ByteBuffer =
        codec.encodeValue(value.asJson.noSpaces)
      override def decodeValue(bytes: ByteBuffer): PubSubMessage =
        decode[PubSubMessage](codec.decodeValue(bytes)).getOrElse(throw new NoSuchElementException)

      override def encodeKey(key: String, target: ByteBuf): Unit =
        codec.encodeKey(key, target)
      override def encodeValue(value: PubSubMessage, target: ByteBuf): Unit =
        codec.encodeValue(value.asJson.noSpaces, target)

      override def estimateSize(keyOrValue: scala.Any): Int = codec.estimateSize(keyOrValue)
    }
}
