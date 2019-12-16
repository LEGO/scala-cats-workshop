package chat.plugins

import cats.effect.{ContextShift, IO}
import chat.OutgoingWebsocketMessage.Message
import chat.plugins.ChatPlugin.{ChatPlugin, PersonalChatPlugin, PublicChatPlugin}
import sttp.client.{Response, SttpBackend}
import sttp.client.asynchttpclient.WebSocketHandler
import sttp.client.asynchttpclient.cats.AsyncHttpClientCatsBackend

object userdefined {

  def allPublicPlugins(implicit cs: ContextShift[IO]): List[PublicChatPlugin] =
    List(emote, cowsay, giphy, markdown, giphyRandom)
  def allPersonalPlugins(implicit cs: ContextShift[IO]): List[PersonalChatPlugin] = List(highlightUser)

  def emote: PublicChatPlugin = ChatPlugin.publicSync { (username, message) =>
    if (message.text.startsWith("/me "))
      message.copy(text = s"$username ${message.text.stripPrefix("/me ")}", isEmote = Some(true))
    else message
  }

  def highlightUser: PersonalChatPlugin = ChatPlugin.personalSync { (currentUser, message) =>
    message match {
      case msg @ Message(_, _, text, _, _) if text.contains(currentUser) =>
        msg.copy(text = msg.text.replace(currentUser, s"<strong>$currentUser</strong>"), isEmote = true)
      case msg => msg
    }
  }

  def cowsay: PublicChatPlugin = ChatPlugin.public { (_, message) =>
    if (message.text.startsWith("/cowsay "))
      IO.delay {
        import sys.process._
        val cowSaid = s"cowsay ${message.text.stripPrefix("/cowsay ")}".!!
        message.copy(text = cowSaid.replaceAll(System.lineSeparator(), " <br>"))
      } else {
      IO.pure(message)
    }
  }

  // A plugin that does nothing to the messages
  def notYetImplemented[T]: ChatPlugin[T] = _ => identity

  /**
    * The following are suggestions for plugins that you could implement.
    */
  /**
    * /gif <search-string>
    *   should find the highest ranked gif based on your search-string
    *   and add the imageUrl to the message.
    */
  def giphy(implicit cs: ContextShift[IO]): PublicChatPlugin = ChatPlugin.public { (username, message) =>
    if (message.text.startsWith("/gif ")) {
      val q = message.text.stripPrefix("/gif ")
      for {
        client <- GiphyApi.makeDefaultClient
        resp   <- client.search(q)
        _        = println(resp.body)
        imageUrl = resp.body.toOption.flatMap(_.data.headOption.flatMap(_.images.get("fixed_height"))).flatMap(_.url)
      } yield (message.copy(text = s"""$username searched for '$q': <br /> <img src="${imageUrl.mkString}"/> """))
    } else {
      IO.pure(message)
    }
  }

  def giphyRandom(implicit cs: ContextShift[IO]): PublicChatPlugin = ChatPlugin.public { (_, message) =>
    if (message.text.startsWith("/giphyrnd"))
      for {
        client <- GiphyApi.makeDefaultClient
        resp   <- client.random
        _        = println(resp.body)
        imageUrl = resp.body.toOption.flatMap(_.data.images.get("fixed_height")).flatMap(_.url)
      } yield (message.copy(text = s""" <img src="${imageUrl.mkString}"/> """))
    else {
      IO.pure(message)
    }
  }

  /**
    * This plugin should run by default and
    * convert text messages from markdown to
    * corresponding HTML.
    *
    * Flex-mark is already included on
    * the classpath for your convenience:
    *
    *    https://github.com/vsch/flexmark-java
    */
  def markdown: PublicChatPlugin = notYetImplemented

  /**
    * Runs the command "/fortune" and displays the output.
    * (fortune is a Unix application that returns a random fortune cookie quote)
    */
  def fortune: PublicChatPlugin = notYetImplemented
}

object GiphyApi {

  def makeDefaultClient(implicit cs: ContextShift[IO]): IO[GiphyApi] =
    AsyncHttpClientCatsBackend[IO]()
      .map { implicit catsBackend =>
        new GiphyApi()
      }
}

class GiphyApi(implicit backend: SttpBackend[IO, Nothing, WebSocketHandler]) {
  import sttp.tapir._
  import sttp.tapir.json.circe._
  import io.circe.generic.auto._
  import sttp.tapir.client.sttp._
  import sttp.client._

  def apiKey: Option[String] = sys.env.get("GIPHY_API_KEY")
  val baseUri                = uri"https://api.giphy.com"

  val randomEndpoint =
    endpoint
      .in("v1" / "gifs" / "random")
      .in(query[String]("api_key"))
      .in(query[String]("rating"))
      .out(jsonBody[GiphyResponse])
      .errorOut(stringBody)

  val searchEndpoint =
    endpoint
      .in("v1" / "gifs" / "search")
      .in(query[String]("api_key"))
      .in(query[String]("rating"))
      .in(query[String]("q"))
      .out(jsonBody[GiphySearchResponse])
      .errorOut(stringBody)

  case class GiphyResponse(
      data: Data,
  )
  case class Data(images: Map[String, Image])
  case class Image(url: Option[String])

  case class GiphySearchResponse(data: List[Data])

  def random: IO[Response[Either[String, GiphyResponse]]] = {
    val request = randomEndpoint.toSttpRequestUnsafe(baseUri)
    request((apiKey.getOrElse(""), "")).send()
  }

  def search(q: String): IO[Response[Either[String, GiphySearchResponse]]] = {
    val request = searchEndpoint.toSttpRequestUnsafe(baseUri)
    request((apiKey.getOrElse(""), "", q)).send()
  }

}
