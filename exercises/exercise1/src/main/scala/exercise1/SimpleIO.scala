package exercise1

object SimpleIO {
  def delay[T](run: => T): SimpleIO[T]                                 = Delay(() => run)
  def pure[T](t: T): SimpleIO[T]                                       = Pure(t)
  def flatMap[T, U](et: SimpleIO[T])(f: T => SimpleIO[U]): SimpleIO[U] = FlatMapped(et, f)
  def map[T, U](eff: SimpleIO[T])(f: T => U): SimpleIO[U]              = FlatMapped[T, U](eff, t => Pure(f(t)))

  /**
    * Optional
    */
  def traverseList[T, U](xs: List[T], f: T => SimpleIO[U]): SimpleIO[List[U]] = TraverseList(xs, f)
  def sequenceList[T](xs: List[SimpleIO[T]]): SimpleIO[List[T]]               = SequenceList(xs)
}

sealed trait SimpleIO[T] {
  def unsafeRunSync: T
  def map[U](f: T => U): SimpleIO[U]
  def flatMap[U](f: T => SimpleIO[U]): SimpleIO[U] = FlatMapped(this, f)
}

case class Delay[T](run: () => T) extends SimpleIO[T] {
  override def unsafeRunSync: T               = ???
  override def map[U](f: T => U): SimpleIO[U] = Delay(() => f(run()))
}

case class Pure[T](t: T) extends SimpleIO[T] {
  override def unsafeRunSync: T               = ???
  override def map[U](f: T => U): SimpleIO[U] = ???
}

case class FlatMapped[T, U](effect: SimpleIO[T], f: T => SimpleIO[U]) extends SimpleIO[U] {
  override def unsafeRunSync: U               = ???
  override def map[A](g: U => A): SimpleIO[A] = ???
}

/**
  Optional
  */
case class TraverseList[T, U](xs: List[T], f: T => SimpleIO[U]) extends SimpleIO[List[U]] {
  override def map[A](ff: List[U] => A): SimpleIO[A] = ???
  override def unsafeRunSync: List[U]                = ???
}

case class SequenceList[T](xs: List[SimpleIO[T]]) extends SimpleIO[List[T]] {
  override def unsafeRunSync: List[T]               = sequenced.unsafeRunSync
  override def map[U](f: List[T] => U): SimpleIO[U] = sequenced.map(f)

  private def sequenced: SimpleIO[List[T]] = ???
}
