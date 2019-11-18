package chat

import cats._
import cats.effect._
import cats.effect.concurrent.Ref
import cats.implicits._
import dev.profunktor.redis4cats.algebra.{PubSubCommands, SetCommands}
import dev.profunktor.redis4cats.domain.RedisChannel
import fs2.Stream

trait Channel {
  def connect(username: String)(implicit t: Timer[IO]): IO[Session]
  def disconnect(session: Session): IO[Unit]
}

class ChatChannel(
    channel: RedisChannel[String],
    localUsers: Ref[IO, Set[String]],
    plugins: Plugins,
    redis: SetCommands[IO, String, String],
    pubsub: PubSubCommands[Stream[IO, *], String, PubSubMessage]
) extends Channel {
  import PubSubMessage._

  val usersSetName = "users"

  val publish: Stream[IO, PubSubMessage] => Stream[IO, Unit] = pubsub.publish(channel)

  def connect(username: String)(implicit t: Timer[IO]): IO[Session] =
    for {
      availableUsername <- findValidUsername(username)
      _                 <- addUsername(availableUsername)
      _                 <- publishJoinMessage(availableUsername)
      initialUserList   <- userList
    } yield {
      val incomingPipe = plugins.publicPipe(availableUsername)
      val outgoingPipe = plugins.personalPipe(availableUsername)
      val publish      = pubsub.publish(channel)
      val subscribe    = pubsub.subscribe(channel)
      new ChatSession(availableUsername, initialUserList, incomingPipe, outgoingPipe, publish, subscribe)
    }

  def disconnect(session: Session): IO[Unit] =
    for {
      _ <- removeUsername(session.username)
      _ <- publishLeaveMessage(session.username)
    } yield ()

  def addUsername(username: String): IO[Unit] =
    localUsers.update(_ + username) >>
      redis.sAdd(usersSetName, username)

  def removeUsername(username: String): IO[Unit] =
    localUsers.update(_ - username) >>
      redis.sRem(usersSetName, username)

  def isUsernameAvailable(username: String): IO[Boolean] =
    redis.sIsMember(usersSetName, username).map(!_)

  val userList: IO[List[String]] =
    redis.sMembers(usersSetName).map(_.toList.sorted)

  def publishJoinMessage(username: String): IO[Unit] =
    Stream
      .eval(userList)
      .map(users => Join(username, users))
      .through(publish)
      .compile
      .drain

  def publishLeaveMessage(username: String): IO[Unit] =
    Stream
      .eval(userList)
      .map(users => Leave(username, users))
      .through(publish)
      .compile
      .drain

  def findValidUsername(username: String): IO[String] =
    Monad[IO].tailRecM((username, 0)) {
      case (username, count) =>
        val uniquerUsername = makeUniquerUsername(username, count)
        isUsernameAvailable(uniquerUsername)
          .map {
            case true  => Right(uniquerUsername)
            case false => Left((username, count + 1))
          }
    }

  def makeUniquerUsername(username: String, count: Int): String =
    if (count == 0) username
    else s"$username ($count)"

  // Remove any local users from redis when we are shutting down
  def shutdown: IO[Unit] =
    localUsers.get.flatMap(_.toList.traverse(removeUsername)).void

}

object ChatChannel {
  def makeResource(
      channel: RedisChannel[String],
      localUsers: Ref[IO, Set[String]],
      plugins: Plugins,
      redis: SetCommands[IO, String, String],
      pubsub: PubSubCommands[Stream[IO, *], String, PubSubMessage]
  ): Resource[IO, ChatChannel] =
    Resource.make(Sync[IO].delay(new ChatChannel(channel, localUsers, plugins, redis, pubsub)))(_.shutdown)
}
