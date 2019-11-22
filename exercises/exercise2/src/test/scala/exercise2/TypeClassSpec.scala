package exercise2

import exercise2.typeclasses.CombineId
import exercise2.typeclasses.syntax._
import org.scalacheck.Arbitrary
import org.specs2.specification.BeforeEach
import org.specs2.{mutable, ScalaCheck}

import scala.util.{Failure, Try}
class TypeClassSpec extends mutable.Specification with ScalaCheck with BeforeEach {

  implicit val arbitraryBusinessInteger: Arbitrary[BusinessInteger] =
    Arbitrary(Arbitrary.arbitrary[Int].map(BusinessInteger.apply))

  /**
    * Check if typeclass has been implemented for better errors
    */
  def before = Try(BusinessInteger.combineBusinessInteger) match {
    case Failure(_: NotImplementedError) => ko
    case _                               =>
  }

  "BusinessIntegers should combine like regular integers" >> prop { (a: BusinessInteger, b: BusinessInteger) =>
    (a |+| b).i must_== (a.i + b.i)
  }

  "BusinessInteger, x, combined with identity should be x" >> prop { x: BusinessInteger =>
    x |+| CombineId[BusinessInteger].id must_== x
    CombineId[BusinessInteger].id |+| x must_== x
  }

  "Combining BusinessIntegers is associative" >> prop { (a: BusinessInteger, b: BusinessInteger, c: BusinessInteger) =>
    a |+| (b |+| c) must_== (a |+| b) |+| c
  }

}
