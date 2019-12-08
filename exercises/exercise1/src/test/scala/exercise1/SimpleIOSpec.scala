package exercise1

import org.specs2._

import SimpleIO._

class SimpleIOSpec extends mutable.Specification {

  "SimpleIO" should {

    "execute an effect" in {
      var changeMe = "NOT CHANGED"
      val effect   = delay { changeMe = "CHANGED!" }
      effect.unsafeRunSync
      changeMe must beEqualTo("CHANGED!")
    }.pendingUntilFixed

    "not run effects eagerly" in {
      var changeMe = "NOT CHANGED"
      val effect   = delay { changeMe = "CHANGED!" }
      changeMe must beEqualTo(changeMe)

      effect.unsafeRunSync // check that something is actually implemented
      ok
    }.pendingUntilFixed

    "reuse effect definition" in {
      var counter = 0
      val effect  = delay { counter = counter + 1 }

      val program = for {
        _ <- effect
        _ <- effect
        _ <- effect
      } yield ()

      program.unsafeRunSync

      counter mustEqual 3
    }.pendingUntilFixed

    "map on flatmapped" in {
      val flatMapped = pure(1330).flatMap(i => pure(i + 5))
      val mapped     = flatMapped.map(_ + 2)

      mapped.unsafeRunSync must_=== 1337
    }.pendingUntilFixed

    "[OPTIONAL] sequence list of effects" in {
      val sequenced: SimpleIO[List[Int]] = sequenceList(List(1, 2, 3).map(i => delay { i * 2 }))
      sequenced.unsafeRunSync must_=== List(1, 2, 3).map(_ * 2)
    }.pendingUntilFixed("optional")

    "[OPTIONAL] traverse list with effect" in {
      val traversed = traverseList(List(1, 2, 3), (i: Int) => delay { i * 2 })
      traversed.unsafeRunSync must_=== List(1, 2, 3).map(_ * 2)
      traversed.map(_.sum).unsafeRunSync must_=== 12
    }.pendingUntilFixed("optional")

  }

}
