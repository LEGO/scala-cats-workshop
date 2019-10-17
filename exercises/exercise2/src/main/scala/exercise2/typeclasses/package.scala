package exercise2

package object typeclasses {

  object Combine {

    /**
      * We call this a "summon" method. It's just a convenience over '''implicitly'''
      */
    def apply[T: Combine]: Combine[T] = implicitly[Combine[T]]
  }

  /**
    * In Scala, a typeclass is basically any trait with
    * a single type parameter. There are special rules
    * built into the language that allows these kinds of
    * traits to be picked up via implicit resolution.
    *
    * We will see how searching for an instance of this
    * trait when we have a type `A` can let us define it
    * in different places.
    *
    * @tparam A The type of elements that we want to combine.
    */
  trait Combine[A] {

    /**
      * Combines two elements of the same type.
      * This operation '''must be associative''', i.e.:
      *
      *  {{{
      *  combine(combine(x,y),z) == combine(x,combine(y,z))
      *  }}}
      *
      * @param x First operand
      * @param y Second operand
      * @return The combination of x and y
      */
    def combine(x: A, y: A): A
  }

  object CombineId {
    def apply[T: CombineId]: CombineId[T] = implicitly[CombineId[T]]
  }
  trait CombineId[A] extends Combine[A] {

    /**
      * This is the left-right-identity element of the operation {{{combine}}}, i.e.:
      *
      * {{{
      *   combine(x,id) == combine(id,x) == x
      * }}}
      */
    def id: A
  }

  object syntax {
    implicit class CombineSyntax[T: Combine](t: T) {
      def |+|(that: T): T = Combine[T].combine(t, that)
    }
  }
}
