val V = new {
  val cats   = "2.0.0"
  val specs2 = "4.6.0"
}

val commonSettings = Seq(
  name := "Scala Cats Workshop",
  scalaVersion := "2.13.1"
)

lazy val root = (project in file(".")).aggregate(exercise1)

lazy val exercise1 = (project in file("exercise1"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % V.cats,
      "org.specs2" %% "specs2-core" % V.specs2 % Test
    )
  )
