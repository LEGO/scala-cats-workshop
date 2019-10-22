package chat

import cats._
import cats.effect._
import cats.implicits._
import dev.profunktor.redis4cats.algebra.{PubSubCommands, SetCommands}
import dev.profunktor.redis4cats.domain.RedisChannel
import fs2.Stream

trait Channel[F[_]] {
  def connect(username: String): F[Session[F]]
  def disconnect(session: Session[F]): F[Unit]
}

class ChatChannel[F[_]: Sync: Timer](
    channel: RedisChannel[String],
    redis: SetCommands[F, String, String],
    pubsub: PubSubCommands[Stream[F, *], String, PubSubMessage]
) extends Channel[F] {
  import PubSubMessage._

  val usersSetName = "users"

  val publish = pubsub.publish(channel)

  def connect(username: String): F[Session[F]] =
    for {
      availableUsername <- findValidUsername(username)
      _                 <- addUsername(availableUsername)
      _                 <- publishJoinMessage(availableUsername)
      initialUserList   <- userList
      publish   = pubsub.publish(channel)
      subscribe = pubsub.subscribe(channel)
    } yield new ChatSession[F](availableUsername, initialUserList, publish, subscribe)

  def disconnect(session: Session[F]): F[Unit] =
    for {
      _ <- removeUsername(session.username)
      _ <- publishLeaveMessage(session.username)
    } yield ()

  def addUsername(username: String): F[Unit] =
    redis.sAdd(usersSetName, username)

  def removeUsername(username: String): F[Unit] =
    redis.sRem(usersSetName, username)

  def isUsernameAvailable(username: String): F[Boolean] =
    redis.sIsMember(usersSetName, username).map(!_)

  val userList: F[List[String]] =
    redis.sMembers(usersSetName).map(_.toList.sorted)

  def publishJoinMessage(username: String): F[Unit] =
    Stream
      .eval(userList)
      .map(users => Join(username, users))
      .to(publish)
      .compile
      .drain

  def publishLeaveMessage(username: String): F[Unit] =
    Stream
      .eval(userList)
      .map(users => Leave(username, users))
      .to(publish)
      .compile
      .drain

  def findValidUsername(username: String): F[String] =
    Monad[F].tailRecM((username, 0)) {
      case (username, count) =>
        val uniquerUsername = makeUniquerUsername(username, count)
        isUsernameAvailable(uniquerUsername).map {
          case true  => Right(username)
          case false => Left((username, count + 1))
        }
    }

  def makeUniquerUsername(username: String, count: Int): String =
    if (count == 0) username
    else s"$username ($count)"

}
