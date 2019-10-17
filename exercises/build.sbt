val V = new {
  val cats   = "2.0.0"
  val specs2 = "4.6.0"
}

val commonSettings = Seq(
  name := "Workshop",
  scalaVersion := "2.13.1"
)

lazy val root = (project in file(".")).aggregate(exercise1)

lazy val exercise1 = (project in file("exercise1"))
  .settings(commonSettings, name += ": Effects")
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core"   % V.cats,
      "org.specs2"    %% "specs2-core" % V.specs2 % Test
    )
  )

lazy val exercise2 = (project in file("exercise2"))
  .settings(commonSettings, name += ": Typeclasses")
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core"         % V.cats,
      "org.specs2"    %% "specs2-core"       % V.specs2 % Test,
      "org.specs2"    %% "specs2-scalacheck" % V.specs2 % Test
    )
  )
  .dependsOn(exercise1)

lazy val exercise3 = (project in file("exercise3"))
  .settings(commonSettings, name += ": Putting it all together (Chat Server)")
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core"   % V.cats,
      "org.specs2"    %% "specs2-core" % V.specs2 % Test
    )
  )
