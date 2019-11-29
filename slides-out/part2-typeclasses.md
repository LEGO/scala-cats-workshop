# Part 2

## Typeclasses

> Typeclasses are **not a language construct in Scala**

> A **Typeclass** is **a design pattern** in **Scala**

----

## Implicits

The typeclass pattern in Scala relies **heavily** on the use of **implicits**. Implicits have many use cases in Scala:

* Implicit values
* Implicit parameters
* Implicit classes (extension methods)
* ~~Implicit conversions~~ (*"deprecated"*)
* **Advanced**:
  * Typeclass induction (advanced)
  * Typelevel programming (advanced)

----


## Implicit resolution

Implicits are resolved via:

* Lexical scope
* Implicit scope (companion objects)
* Implicit scope of *type arguments* (including `F` and `T` in `F[T]`)
* Package objects

Many quirks: priorities, ambiguities, subtyping, etc.

----

Only **implicit values** and **implicit parameters** are needed to encode a typeclass.

![](images/phew.webp) <!-- .element height="50%" width="50%" -->

----

### Lexical Scope

**Define** an implicit value

```scala
implicit val number:Int = 39
// number: Int = 39
```

***Resolve*** an implicit value from local scope

```scala
def addNumber(m:Int)(implicit n:Int):Int = m + n
addNumber(3)
// res0: Int = 42
```

----

### Lexical Scope with Subtyping

**Define** an implicit value

```scala
trait Foo
case object Bar extends Foo
implicit val number = Bar
// number: Bar.type = Bar
```

***Resolve*** an implicit value **of a subtype** from local scope

```scala
def fetch(implicit f:Foo):Foo = f
fetch
// res2: Foo = Bar
```

----

### Implicit Scope

```scala
object MyInt {
  implicit val default: MyInt = MyInt(1337)
}
case class MyInt(i:Int)

def fetch(implicit m:MyInt): MyInt = m  
fetch
// res4: MyInt = MyInt(1337)
```

----

### Implicit Scope with Generic Types

```scala
trait Show[T] {
  def show(t:T):String
}
object Show {
  implicit val showInt:Show[Int] = i => s"THIS IS MY INT -->$i<--"
}

def show[T](t: T)(implicit s: Show[T]): String = s.show(t)

show(11)
// res6: String = "THIS IS MY INT -->11<--"
```

----

That last one...


----

**is a typeclass!**

![](images/woah.webp)


----

We can add a little bit of syntactic sugar by **moving our method** into an **implicit class**:

```scala
implicit class ShowOps[T](t: T){
  def show(implicit s:Show[T]): String = s.show(t)
}

42.show
// res7: String = "THIS IS MY INT -->42<--"
```

----

Let's look at another typeclass:

```scala
trait Distance[T]{
  def distanceTo(x: T, y: T): Double
}

case class Point(x:Double, y:Double)
object Point {
  import math._
  implicit val distanceTo: Distance[Point] = 
    (a, b) => sqrt( pow( b.x-a.x, 2) + pow( b.y-a.y, 2 ))
}
```

----

We can add special **infix syntax** to our typeclass like so:

```scala
implicit class DistanceOps[T](t:T){
  implicit def <->(u:T)(implicit d: Distance[T]): Double = d.distanceTo(t, u)  
}

Point(1, 2) <-> Point(3, 4) 
// res8: Double = 2.8284271247461903
```

----

If you have special syntax for a typeclass, **context bounds** gives you convenient syntax sugar:

```scala
implicit def distance1[T]
  (a:T, b:T)(implicit d: Distance[T]): Double = a <-> b
```

is equivalent to:

```scala
implicit def distance2[T: Distance]
  (a:T, b:T):Double = a <-> b
```

> Notice that the syntax is picked up automatically, because implicit class `DistanceOps[T]` gets desugared and added to **the companion object of `DistanceOps`**

