package exercise2

import exercise1.SpecialFx
import exercise1.SpecialFx._
import exercise2.typeclasses.CombineId
import exercise2.typeclasses.syntax._

object Exercise2Main extends App {

  def getNumber: SpecialFx[BusinessInteger] =
    for {
      _ <- delay { print("Please type an integer: ") }
      i <- delay { io.StdIn.readInt() }
    } yield BusinessInteger(i)

  def getNumbers(n: Int): SpecialFx[List[BusinessInteger]] = n match {
    case _ if n > 0 =>
      for {
        n  <- getNumber
        ns <- getNumbers(n.i - 1)
      } yield n :: ns
    case 0 => pure(Nil)
  }

  private def intersperse[T](ts: List[T])(inject: T): List[T] =
    ts match {
      case a :: b :: rest => a :: inject :: intersperse(b :: rest)(inject)
      case _              => ts
    }

  private def combineIntersperse[T: CombineId](ts: List[T])(inject: T): T =
    intersperse(ts)(inject).foldLeft(CombineId[T].id)(_ |+| _)

  val combineProgram: SpecialFx[BusinessInteger] = for {
    _                <- delay { print(s"How many numbers do you want to combine?: ") }
    n                <- delay { io.StdIn.readInt }
    businessIntegers <- getNumbers(n)
    numbersAsString = combineIntersperse(businessIntegers.map(_.i.toString))(inject = ", ")
    sum             = fold(businessIntegers)
    _ <- delay { println(s"The combination of [$numbersAsString] is: $sum") }
  } yield sum

  combineProgram.unsafeRunSync
}
