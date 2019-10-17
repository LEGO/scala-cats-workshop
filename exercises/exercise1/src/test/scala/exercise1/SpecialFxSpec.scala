package exercise1

import org.specs2._

import SpecialFx._

class SpecialFxSpec extends mutable.Specification {

  "SpecialFx" should {

    "execute an effect" in {
      var changeMe = "NOT CHANGED"
      val effect   = delay { changeMe = "CHANGED!" }
      effect.unsafeRunSync
      changeMe must beEqualTo("CHANGED!")
    }

    "not run effects eagerly" in {
      var changeMe = "NOT CHANGED"
      val effect   = delay { changeMe = "CHANGED!" }
      changeMe must beEqualTo(changeMe)
    }

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
    }

  }

}
