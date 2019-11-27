# scala-cats-workshop
This is the exercise material for a Scala and Cats workshop


## Layout of the repository

The workshop consists of slides and exercises. The exercises live in the `exercises/` folder, and it is an SBT project with 3 sub-projects, 1 for each exercise.

The slides are built from sources in `slides/` using [mdoc](https://scalameta.org/mdoc/) followed by [reveal-md](https://github.com/webpro/reveal-md). `mdoc` ensures that all code examples compile and typecheck correctly. It also provides inline output as comments inside the code snippets, e.g.:

```scala
println("Hello, World!")
// Hello, World!
```

## Slides

You can see the latest build of the slides at [https://lego.github.io/scala-cats-workshop/](https://lego.github.io/scala-cats-workshop/)