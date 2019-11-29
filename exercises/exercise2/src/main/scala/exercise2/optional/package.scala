package exercise2

import cats.Id

package object optional {

  /**
    * Higher kinded types is a fancy word for generic types like List[T], Option[T],
    * Either[L,R], Map[K,V], etc. They are the type-level equivalent to higher-order functions.
    * Sometimes, they are called type constructors because they are not considered a type
    * before their respective type parameters have been defined as concrete types, e.g. List[Int] or Map[String,Double].
    *
    * You can use such a type as a type parameter itself.
    */
  def higherKindIdentity[F[_], T](f: F[T]): F[T] = f

  higherKindIdentity(List(1, 2, 3))

  /**
    * Cats has a "silly" higher kinded type called `Id`.
    *
    * It is defined as `type Id[T] = T`. It allows us to use
    * a function that expects a higher kinded type as if it
    * was just expecting a plain parameter
    */
  higherKindIdentity[Id, Int](42)

  /**
    * With this capability, you should be able to make a typeclass that works for higher kinded types
    * and indeed - you can! This is pervasive throughout the Cats codebase and allows you
    * to take another step up in terms of abstraction level.
    */
  trait Constructible[F[_]] {
    def construct[T](t: T): F[T]
  }

  implicit val listConstructible: Constructible[List] = new Constructible[List] {
    override def construct[T](t: T): List[T] = List(t)
  }

  implicit val optionConstructible: Constructible[Option] = new Constructible[Option] {
    override def construct[T](t: T): Option[T] = ???
  }

  type EitherFixedLeft[R] = Either[String, R]
  implicit val eitherConstructible: Constructible[EitherFixedLeft] = new Constructible[EitherFixedLeft] {
    override def construct[T](t: T): EitherFixedLeft[T] = ???
  }

  def constructAbstractF[F[_]: Constructible, T](t: T): F[T] =
    implicitly[Constructible[F]].construct(t)

  /**
    * In this way, we can abstract away constructors -- neat!
    */
  val listOf42: List[Int] = constructAbstractF[List, Int](42)

  /**
    * We can abstract away the map function as well
    */
  trait Mappable[F[_]] {
    def map[T, U](ft: F[T])(f: T => U): F[U]
  }

  implicit val listMappable: Mappable[List] = new Mappable[List] {
    override def map[T, U](ft: List[T])(f: T => U): List[U] = ft.map(f)
  }
  implicit val eitherMappable: Mappable[EitherFixedLeft] = new Mappable[EitherFixedLeft] {
    override def map[T, U](ft: EitherFixedLeft[T])(f: T => U): EitherFixedLeft[U] = ???
  }

  /**
    * Now we can map on "some kind of thing that produces T"
    */
  def mapOnF[F[_]: Mappable, T, U](ft: F[T])(f: T => U): F[U] =
    implicitly[Mappable[F]].map(ft)(f)

  val multiplied: List[Int] = mapOnF(List(1, 2, 3))(_ * 2)
}
