package exercise2

import exercise1.SimpleIO
import exercise1.SimpleIO._
import exercise2.typeclasses.CombineId
import exercise2.typeclasses.syntax._

object Exercise2Main extends App {

  def getNumber: SimpleIO[BusinessInteger] =
    for {
      _ <- delay { print("Please type an integer: ") }
      i <- delay { io.StdIn.readInt() }
    } yield BusinessInteger(i)

  def getNumbers(numbersToGet: Int): SimpleIO[List[BusinessInteger]] =
    if (numbersToGet <= 0) pure(Nil)
    else {
      for {
        n  <- getNumber
        ns <- getNumbers(numbersToGet - 1)
      } yield n :: ns
    }

  private def intersperse[T](ts: List[T])(inject: T): List[T] =
    ts match {
      case a :: b :: rest => a :: inject :: intersperse(b :: rest)(inject)
      case _              => ts
    }

  private def combineIntersperse[T: CombineId](ts: List[T])(inject: T): T =
    intersperse(ts)(inject).foldLeft(CombineId[T].id)(_ |+| _)

  val combineProgram: SimpleIO[BusinessInteger] = for {
    _                <- delay { print(s"How many numbers do you want to combine?: ") }
    n                <- delay { io.StdIn.readInt }
    businessIntegers <- getNumbers(n)
    numbersAsString = combineIntersperse(businessIntegers.map(_.i.toString))(inject = ", ")
    sum             = fold(businessIntegers)
    _ <- delay { println(s"The combination of [$numbersAsString] is: $sum") }
  } yield sum

  combineProgram.unsafeRunSync
}
