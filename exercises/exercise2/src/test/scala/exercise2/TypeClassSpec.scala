package exercise2

import exercise2.typeclasses.CombineId
import org.scalacheck.Arbitrary
import org.specs2.{mutable, ScalaCheck}
import exercise2.typeclasses.syntax._
class TypeClassSpec extends mutable.Specification with ScalaCheck {

  implicit val arbitraryBusinessInteger: Arbitrary[BusinessInteger] =
    Arbitrary(Arbitrary.arbitrary[Int].map(BusinessInteger.apply))

  "BusinessIntegers should combine like regular integers" >> prop { (a: BusinessInteger, b: BusinessInteger) =>
    (a <+> b).i must_== (a.i + b.i)
  }

  "BusinessInteger, x, combined with identity should be x" >> prop { x: BusinessInteger =>
    x <+> CombineId[BusinessInteger].id must_== x
    CombineId[BusinessInteger].id <+> x must_== x
  }

  "Combining BusinessIntegers is associative" >> prop { (a: BusinessInteger, b: BusinessInteger, c: BusinessInteger) =>
    a <+> (b <+> c) must_== (a <+> b) <+> c
  }

}
