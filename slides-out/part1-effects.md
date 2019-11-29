# Part 1

## Effects

----

> Functional programmers **don't like side effects**


![](images/talk_about_it.gif)


----

* **Ordering** of **side effects** is difficult to reason about
  * Dijkstra said so ([Go To Statement Considered Harmful](https://homepages.cwi.nl/~storm/teaching/reader/Dijkstra68.pdf))  ![](images/dijkstra.png)
* **Programs without side effects are useless** (and un-observable)
* We can **just pretend that *Scala* doesn't have them!**
  * With discipline, we can get *referential transparency*

----

## Referential Transparency

When we program with side-effects, we lose *referential transparency:*

```scala
case class Number(var n:Int){
  def add(m:Int): Number = {
      n = n + m
      this
  }
}

val x = Number(39)
// x: Number = Number(45)

val y = x add 3
// y: Number = Number(45)

y == Number(42) // Yay
// res0: Boolean = true // Yay

(x add 3) == Number(42) //Yikes!
// res1: Boolean = false
```

----

Some things are *easier for our brains* if stuff doesn't change:

```scala
case class Number(n:Int){
  def add(m:Int): Number = Number(n + m)
}

val x = Number(39)
// x: Number = Number(39)

val y = x add 3
// y: Number = Number(42)

y == Number(42) // Yay
// res3: Boolean = true // Yay

(x add 3) == Number(42) //Yay 😎 !
// res4: Boolean = true
```

----

## Referentially transparent effects


```scala
def app = for {
  _ <- Future { println("Yay!") }
  _ <- Future { println("Yay!") }
  _ <- Future { println("Yay!") }
} yield ()

Await.result(app,500.millis)
// Yay!
// Yay!
// Yay!
```

----

<span style="font-size:3em">🤔</span> lots of code duplication...**refactor**!

> **Common Subexpression Elimination:** *extract syntactically equivalent expression into constant*

----

```scala
def app = { 
  val printYay = Future { println("Yay!") }
  for {
    _ <- printYay
    _ <- printYay
    _ <- printYay
  } yield ()
}

Await.result(app, 500.millis)
// Yay!
```

<div class="fragment">
<img src="images/but_why.webp"/> <span style="font-size:5em">🤷</span>
</div>
----

![](images/cats_effect_impure.png)
(**CREDIT**: [Impure Pics](https://impurepics.com))

----

> *Future* is **not referentially transparent**.

Requires you to understand about *how Scala evaluates your expressions* <span style="font-size:3em">🤯</span>

----

## Enter: **Cats**

![](images/enter_cats.gif) <!-- .element height="50%" width="50%" -->

----

[Cats](https://typelevel.org/cats/)

> Lightweight, modular, and extensible library for functional programming

![](images/cats_commutativity_diagram.png)

----

# [Cats Effect](https://typelevel.org/cats-effect/)

> The IO Monad for Scala

Cats effect is both an *"interface"* and an *implementation* of referentially transparent effects in Scala.

----

```scala
import cats.effect._
```

```scala
val app = for {
  _ <- IO.delay{ println("Yay!") }
  _ <- IO.delay{ println("Yay!") }
  _ <- IO.delay{ println("Yay!") }
} yield ()
// app: IO[Unit] = Bind(Delay(<function0>), <function1>)
```


<span style="font-size:3em">🤔</span> no side-effects...**at all!?**


----

```scala
val app = for {
  _ <- IO.delay{ println("Yay!") }
  _ <- IO.delay{ println("Yay!") }
  _ <- IO.delay{ println("Yay!") }
} yield ()
// app: IO[Unit] = Bind(Delay(<function0>), <function1>)

app.unsafeRunSync()
// Yay!
// Yay!
// Yay!
```

<span style="font-size:3em">🤔</span> lots of code duplication again...<span style="font-size:3em">😨</span>**refactor**?

----

```scala
val printYay = IO.delay{ println("Yay!")  }
// printYay: IO[Unit] = Delay(<function0>)

val app = for {
  _ <- printYay
  _ <- printYay
  _ <- printYay
} yield ()
// app: IO[Unit] = Bind(Delay(<function0>), <function1>)
```

...drum-roll please <span style="font-size:3em">🥁</span>

----

```scala
app.unsafeRunSync()
// Yay!
// Yay!
// Yay!
```

![](images/thumbs_up.webp)

----

# Exercise 1

Build your own effect type.

```
├── exercises
│   ├── build.sbt
│   ├── exercise1
│   │   └── src
│   │       ├── main
│   │       │   └── scala
│   │       │       └── exercise1
│   │       │           ├── Exercise1Main.scala
│   │       │           └── SpecialFx.scala
│   │       └── test
│   │           └── scala
│   │               └── exercise1
│   │                   └── SpecialFxSpec.scala
```

