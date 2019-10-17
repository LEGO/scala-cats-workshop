package exercise1

import SpecialFx._
object Exercise1Main extends App {

  val greetingProgram: SpecialFx[String] = for {
    _    <- delay { println("Hello, who are you?") }
    name <- delay { io.StdIn.readLine("> Type your name: ") }
    _    <- delay { println(s"Nice to meet you, $name!") }
  } yield name

  /**
    * We call this `end of the world`, i.e. it's the
    * limit of our control. Everything else than this line
    * can represent an internally consistent stateless functional
    * program, allowing us the freedom of referential transparency.
    *
    * Without this line, or entire program has no side effects.
    */
  greetingProgram.unsafeRunSync

}
