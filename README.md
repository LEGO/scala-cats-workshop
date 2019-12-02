# scala-cats-workshop
This is the exercise material for a Scala and Cats workshop


## Layout of the repository

```bash
.
├── docs # the compiled slides, available on the github page
│   ├── _assets
│   ├── css
│   ├── images
│   ├── js
│   ├── lib
│   └── plugin
├── exercises     # All exercises
│   ├── client-app  # JavaScript client used in exercise3 (ignore)
│   ├── exercise1   # Effects
│   ├── exercise2   # Typeclasses 
│   ├── exercise3   # Putting it all together
│   └── project     # Sbt settings     
└── slides # Sources for the slides
    ├── css
    └── images
```

The workshop consists of slides and exercises. The exercises live in the `exercises/` folder, and it is an SBT project with 3 sub-projects, 1 for each exercise.

The slides are built from sources in `slides/` using [mdoc](https://scalameta.org/mdoc/) followed by [reveal-md](https://github.com/webpro/reveal-md). `mdoc` ensures that all code examples compile and typecheck correctly. It also provides inline output as comments inside the code snippets, e.g.:

```scala
println("Hello, World!")
// Hello, World!
```

## Slides

You can see the latest build of the slides at [https://lego.github.io/scala-cats-workshop/](https://lego.github.io/scala-cats-workshop/)

## Exercises

### Installing/Running SBT 

On Mac OS, you can simply use the script inside the `exercises` folder, but on Windows you need to install 
SBT locally on your machine.

Open powershell and install [scoop](https://scoop.sh/):

```powershell
iwr -useb get.scoop.sh | iex
```


After this, install git, java, and SBT:

```powershell
scoop install git
scoop bucket add java
scoop install openjdk
scoop install sbt
```



### Running the exercises

We use [SBT](https://www.scala-sbt.org/) as a build tool for the exercises. 
SBT is an interactive tool, meaning that it's meant to be a long-running process with user interaction -- it is NOT a batch tool. To start SBT, go into the root folder `./exercises/` (root folder contains `build.sbt` file). Once there, run `./sbt` or on Windows use SBT from your local system `sbt`.
This script bootstraps SBT and starts it using the build definition in the current folder.

The process would look something like this:

```
> scala-cats-workshop → λ git master* → cd exercises
> exercises → λ git master* → ./sbt
Downloading sbt launcher for 1.3.3:
  From  https://repo.scala-sbt.org/scalasbt/maven-releases/org/scala-sbt/sbt-launch/1.3.3/sbt-launch-1.3.3.jar
    To  /Users/dkfepaha/.sbt/launchers/1.3.3/sbt-launch.jar
/Users/dkfepaha/.sbt/launchers/1.3.3/sbt-launch.jar: OK
[info] Loading settings for project global-plugins from credentials.sbt,metals.sbt ...
[info] Loading global plugins from /Users/dkfepaha/.sbt/1.0/plugins
[info] Loading settings for project exercises-build from plugins.sbt ...
[info] Loading project definition from /Users/dkfepaha/repos/scala-cats-workshop/exercises/project
[info] Loading settings for project root from build.sbt ...
[info] Set current project to root (in build file:/Users/dkfepaha/repos/scala-cats-workshop/exercises/)
[info] sbt server started at local:///Users/dkfepaha/.sbt/1.0/server/4cc4076b16db6d46d32f/sock
sbt:root>
```

You are now in the SBT shell which lets you interact with the build and your progarms. Typing `projects` shows all the sub-projects:

```bash
sbt:root> projects
[info] In file:/Users/dkfepaha/repos/scala-cats-workshop/exercises/
[info] 	   exercise1
[info] 	   exercise2
[info] 	   exercise3
[info] 	 * root
sbt:root>
```

Place yourself in `exercise1` by issuing the following command:

```
sbt:root> project exercise1
[info] Set current project to Workshop: Effects (in build file:/Users/dkfepaha/repos/scala-cats-workshop/exercises/)
sbt:Workshop: Effects>
```

Now, you can run the tests like so:

```
sbt:Workshop: Effects> test
[info] Compiling 1 Scala source to /Users/dkfepaha/repos/scala-cats-workshop/exercises/exercise1/target/scala-2.13/classes ...
[info] Compiling 1 Scala source to /Users/dkfepaha/repos/scala-cats-workshop/exercises/exercise1/target/scala-2.13/test-classes ...
[info] SpecialFxSpec
[info] SpecialFx should
[error]   x execute an effect
[error]    an implementation is missing (SpecialFx.scala:16)
[info]   + not run effects eagerly
[error]   x reuse effect definition
[error]    an implementation is missing (SpecialFx.scala:24)
[info] Total for specification SpecialFxSpec
[info] Finished in 165 ms
[info] 3 examples, 2 failures, 0 error
[error] Failed: Total 3, Failed 2, Errors 0, Passed 1
[error] Failed tests:
[error] 	exercise1.SpecialFxSpec
[error] (Test / test) sbt.TestsFailedException: Tests unsuccessful
[error] Total time: 7 s, completed Nov 29, 2019 11:52:54 AM
sbt:Workshop: Effects>
```

As you can see, the tests are failing. **This is to be expected** as we havent solved the exercise yet. 
The intention is for you to provide the missing implementations (marked as `???` in the code) until the tests pass.
To watch for source changes and re-run the tests automatically, you can use the `~` watch command in SBT:

```bash
sbt:Workshop: Effects> ~test
[info] SpecialFxSpec
[info] SpecialFx should
[error]   x execute an effect
[error]    an implementation is missing (SpecialFx.scala:16)
<...>
[info] 1. Monitoring source files for exercise1/test...
[info]    Press <enter> to interrupt or '?' for more options.
```

### Running the main program 

Each exercise contains a small _main program_ that uses your implementation to work.
These programs serve as simple examples showing the usage of what you made. To run a main program, use the following command:

> **SPOILER ALERT**

Running exercise 1 is done like so:

```
sbt:Workshop: Effects> run
[info] running exercise1.Exercise1Main
Hello, who are you?
> Type your name: Felix
Nice to meet you, Felix!
[success] Total time: 2 s, completed Nov 29, 2019 12:09:34 PM
sbt:Workshop: Effects>
```
