import exercise2.typeclasses.{Combine, CombineId}

package object exercise2 {

  object BusinessInteger {
    implicit val combineBusinessInteger: CombineId[BusinessInteger] = new CombineId[BusinessInteger] {
      override def combine(x: BusinessInteger, y: BusinessInteger): BusinessInteger = BusinessInteger(x.i + y.i)
      override def id: BusinessInteger                                              = BusinessInteger(0)
    }
  }

  implicit val combineString: CombineId[String] = new CombineId[String] {
    override def combine(x: String, y: String): String = x + y
    override def id: String                            = ""
  }

  /**
    * This is a very special integer that the business wants
    * you to implement. It doesn't do anything special, but it
    * has a fancy name.
    */
  final case class BusinessInteger(i: Int)

  /**
    * Combines two elements of type {{{T}}} using the fact that there is an
    * instance of the typeclass {{{Combine[T]}}} in '''implicit scope'''.
    *
    * '''TIP:''' use the syntax of the {{{Combine[_]}}} typeclass to make
    * this definition concise.
    */
  def add[T: Combine](x: T, y: T): T = {
    import typeclasses.syntax._
    x <+> y
  }

  /**
    * Combines all the elements of type {{{T}}} in a list of Ts using
    * the fact that there is an instance of typeclass {{{CombineId[T]}}} in '''implicit scope'''.
    *
    * '''TIP:''' use one of the summon methods. Try to use the definition of 'add' from above also.
    * Alternatively, you may use an inline anonymous function.
    */
  def fold[T: CombineId](ts: List[T]): T = ts.fold(CombineId[T].id)(add)

}
