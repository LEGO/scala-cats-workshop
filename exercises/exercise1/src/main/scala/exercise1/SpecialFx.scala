package exercise1

object SpecialFx {
  def delay[T](run: => T): SpecialFx[T]                                   = Delay(() => run)
  def pure[T](t: T): SpecialFx[T]                                         = Pure(t)
  def flatMap[T, U](et: SpecialFx[T])(f: T => SpecialFx[U]): SpecialFx[U] = FlatMapped(et, f)
  def map[T, U](eff: SpecialFx[T])(f: T => U): SpecialFx[U]               = FlatMapped[T, U](eff, t => Pure(f(t)))

  /**
    * Optional
    */
  def traverseList[T, U](xs: List[T], f: T => SpecialFx[U]): SpecialFx[List[U]] = TraverseList(xs, f)
  def sequenceList[T](xs: List[SpecialFx[T]]): SpecialFx[List[T]]               = SequenceList(xs)
}

sealed trait SpecialFx[T] {
  def unsafeRunSync: T
  def map[U](f: T => U): SpecialFx[U]
  def flatMap[U](f: T => SpecialFx[U]): SpecialFx[U] = FlatMapped(this, f)
}

case class Delay[T](run: () => T) extends SpecialFx[T] {
  override def unsafeRunSync: T                = ???
  override def map[U](f: T => U): SpecialFx[U] = Delay(() => f(run()))
}

case class Pure[T](t: T) extends SpecialFx[T] {
  override def unsafeRunSync: T                = ???
  override def map[U](f: T => U): SpecialFx[U] = ???
}

case class FlatMapped[T, U](effect: SpecialFx[T], f: T => SpecialFx[U]) extends SpecialFx[U] {
  override def unsafeRunSync: U                = ???
  override def map[A](g: U => A): SpecialFx[A] = ???
}

/**
  Optional
  */
case class TraverseList[T, U](xs: List[T], f: T => SpecialFx[U]) extends SpecialFx[List[U]] {
  override def map[A](ff: List[U] => A): SpecialFx[A] = ???
  override def unsafeRunSync: List[U]                 = ???
}

case class SequenceList[T](xs: List[SpecialFx[T]]) extends SpecialFx[List[T]] {
  override def unsafeRunSync: List[T]                = sequenced.unsafeRunSync
  override def map[U](f: List[T] => U): SpecialFx[U] = sequenced.map(f)

  private def sequenced: SpecialFx[List[T]] = ???
}
