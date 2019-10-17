package exercise1

object SpecialFx {
  def delay[T](run: => T): SpecialFx[T]                                   = Delay(() => run)
  def pure[T](t: T): SpecialFx[T]                                         = Pure(t)
  def flatMap[T, U](et: SpecialFx[T])(f: T => SpecialFx[U]): SpecialFx[U] = FlatMapped(et, f)
}

sealed trait SpecialFx[T] {
  def unsafeRunSync: T
  def map[U](f: T => U): SpecialFx[U]
  def flatMap[U](f: T => SpecialFx[U]): SpecialFx[U] = FlatMapped(this, f)
}

case class Delay[T](run: () => T) extends SpecialFx[T] {
  override def unsafeRunSync: T                = run()
  override def map[U](f: T => U): SpecialFx[U] = Delay(() => f(run()))
}
case class Pure[T](t: T) extends SpecialFx[T] {
  override def unsafeRunSync: T                = t
  override def map[U](f: T => U): SpecialFx[U] = Pure(f(t))
}
case class FlatMapped[T, U](effect: SpecialFx[T], f: T => SpecialFx[U]) extends SpecialFx[U] {
  override def unsafeRunSync: U                = f(effect.unsafeRunSync).unsafeRunSync
  override def map[A](g: U => A): SpecialFx[A] = FlatMapped(effect, f andThen (_.map(g)))
}
