package chat.plugins

import cats.effect.IO
import chat.OutgoingWebsocketMessage.Message
import chat.plugins.ChatPlugin.{ChatPlugin, PersonalChatPlugin, PublicChatPlugin}

object userdefined {

  val allPublicPlugins: List[PublicChatPlugin]     = List(emote, cowsay, giphy, markdown)
  val allPersonalPlugins: List[PersonalChatPlugin] = List(highlightUser)

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
  def giphy: PublicChatPlugin = notYetImplemented

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
