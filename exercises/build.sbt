val V = new {
  val cats            = "2.0.0"
  val circe           = "0.12.3"
  val circeDerivation = "0.12.0-M7"
  val fs2             = "2.1.0"
  val http4s          = "0.21.0-M5"
  val kindProjector   = "0.11.0"
  val log4cats        = "1.0.1"
  val logback         = "1.2.3"
  val redis4cats      = "0.9.1"
  val specs2          = "4.8.0"
  val tapir           = "0.12.6"
  val sttp            = "2.0.0-RC3"
  val silencer        = "1.4.4"
}

val commonSettings = Seq(
  name := "Workshop",
  scalaVersion := "2.13.1",
)

Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val root = (project in file(".")).aggregate(exercise1, exercise2, exercise3)

lazy val exercise1 = (project in file("exercise1"))
  .settings(commonSettings, name += ": Effects")
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core"   % V.cats,
      "org.specs2"    %% "specs2-core" % V.specs2 % Test,
    ),
  )

lazy val exercise2 = (project in file("exercise2"))
  .settings(commonSettings, name += ": Typeclasses")
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core"         % V.cats,
      "org.specs2"    %% "specs2-core"       % V.specs2 % Test,
      "org.specs2"    %% "specs2-scalacheck" % V.specs2 % Test,
    ),
  )
  .dependsOn(exercise1)

lazy val exercise3 = (project in file("exercise3"))
  .settings(commonSettings, name += ": Putting it all together (Chat Server)")
  .settings(
    libraryDependencies ++= Seq(
      "co.fs2"                       %% "fs2-io"                         % V.fs2,
      "dev.profunktor"               %% "redis4cats-effects"             % V.redis4cats,
      "dev.profunktor"               %% "redis4cats-streams"             % V.redis4cats,
      "dev.profunktor"               %% "redis4cats-log4cats"            % V.redis4cats,
      "io.chrisdavenport"            %% "log4cats-slf4j"                 % V.log4cats,
      "io.circe"                     %% "circe-derivation"               % V.circeDerivation,
      "io.circe"                     %% "circe-parser"                   % V.circe,
      "org.http4s"                   %% "http4s-blaze-server"            % V.http4s,
      "org.http4s"                   %% "http4s-dsl"                     % V.http4s,
      "org.http4s"                   %% "http4s-server"                  % V.http4s,
      "com.softwaremill.sttp.tapir"  %% "tapir-core"                     % V.tapir,
      "com.softwaremill.sttp.tapir"  %% "tapir-json-circe"               % V.tapir,
      "com.softwaremill.sttp.tapir"  %% "tapir-sttp-client"              % V.tapir,
      "com.softwaremill.sttp.client" %% "async-http-client-backend-cats" % V.sttp,
      "com.vladsch.flexmark"         % "flexmark"                        % "0.50.44",
      compilerPlugin("org.typelevel"   %% "kind-projector" % V.kindProjector cross CrossVersion.full),
      compilerPlugin("com.github.ghik" % "silencer-plugin" % V.silencer cross CrossVersion.full),
      "com.github.ghik" % "silencer-lib"    % V.silencer % Provided cross CrossVersion.full,
      "ch.qos.logback"  % "logback-classic" % V.logback,
    ),
  )
