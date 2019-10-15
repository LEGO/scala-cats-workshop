# Part 1

## Effects

> Functional programmers **don't like side effects**


![](../slides/images/talk_about_it.gif)


----

* **Ordering** of **side effects** is difficult to reason about
  * Dijkstra said so ([Go To Statement Considered Harmful](https://homepages.cwi.nl/~storm/teaching/reader/Dijkstra68.pdf))
  * ![](images/dijkstra.png)
* **Programs without side effects are useless** (and un-observable)
* We can **just pretend that *Scala* doesn't have them!** 
  * With discipline, we can get *referential transparency*

----

## Referential Transparency

When we program with side-effects, we lose *referential transparency:*

```scala mdoc
case class Number(var n:Int){
  def add(m:Int): Number = {
      n = n + m
      this
  }
}

val x = Number(39)

val y = x add 3

y == Number(42) // Yay

(x add 3) == Number(42) //Yikes!

```

----

Some things are *easier for our brains* if stuff doesn't change:

```scala mdoc:reset

case class Number(n:Int){
  def add(m:Int): Number = Number(n + m)
}

val x = Number(39)

val y = x add 3

y == Number(42) // Yay

(x add 3) == Number(42) //Yay 😎 !

```

----

## Referential transparent effects


```scala mdoc:reset:invisible
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.duration._
```

```scala mdoc
def app: Future[Unit] = for {
  _ <- Future { println("Yay!") }
  _ <- Future { println("Yay!") }
  _ <- Future { println("Yay!") }
} yield ()

lazy val result = Await.result(app,500.millis)
```

----

```scala mdoc:reset:invisible
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.duration._
```

```scala mdoc


def app: Future[Unit] = { 
  val printYay = Future {
    println("Yay!")
  }
  for {
    _ <- printYay
    _ <- printYay
    _ <- printYay
  } yield ()
}

lazy val result = Await.result(app, 500.millis)
```

----