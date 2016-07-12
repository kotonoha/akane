import com.trueaccord.scalapb.{ScalaPbPlugin => PB}

val scalaPbVersion = "0.5.32"

def pbScala(): Seq[Setting[_]] = {
  val config = PB.protobufSettings ++ Seq(
    PB.flatPackage in PB.protobufConfig := true,
    PB.javaConversions in PB.protobufConfig := true,
    PB.scalapbVersion := scalaPbVersion,
    PB.runProtoc in PB.protobufConfig := (args =>
      com.github.os72.protocjar.Protoc.runProtoc("-v300" +: args.toArray))
  )

  val runtimeDep =
    libraryDependencies += "com.trueaccord.scalapb" %% "scalapb-runtime" % scalaPbVersion % PB.protobufConfig

  config ++ Seq(
    runtimeDep
  )
}

lazy val akaneSettings = Seq(
  organization := "ws.kotonoha",
  moduleName := "akane",
  crossScalaVersions := Seq("2.11.8"),
  scalaVersion := "2.11.8",
  name := "Akane",
  version := "0.2-SNAPSHOT",

  javacOptions ++= Seq("-encoding", "utf8")
)


def akaneProject(projName: String, basePath: File) = {
  val id = s"akane-$projName"

  val localSettings = Def.settings(
    name := s"Akane ${projName.capitalize}",
    moduleName := id
  )

  val allSettings = akaneSettings ++ localSettings ++ commonDeps

  Project(id = id, base = basePath, settings = allSettings)
}

lazy val akaneDeps = Seq(
  //test

  "net.liftweb" %% "lift-json" % "2.6.3",
  "com.jsuereth" %% "scala-arm" % "1.4",

  "com.github.scala-incubator.io" %% "scala-io-core" % "0.4.3",
  "com.github.scala-incubator.io" %% "scala-io-file" % "0.4.3",

  "com.nativelibs4java" % "bridj" % "0.7.0",

  "com.typesafe" % "config" % "1.3.0",
  "com.typesafe.akka" %% "akka-actor" % "2.4.6"
)

lazy val commonDeps = Def.settings(
  libraryDependencies ++= Seq(
    "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",

    "org.scalatest" %% "scalatest" % "2.2.6" % Test,
    "ch.qos.logback" % "logback-classic" % "1.1.7" % Test
  )
)

lazy val akkaDeps = Def.settings(
  libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.4.6"
)

lazy val macroDeps = Def.settings(
  libraryDependencies ++= Seq(
    "org.scala-lang" % "scala-reflect" % scalaVersion.value,
    "org.scalamacros" %% "resetallattrs" % "1.0.0-M1"
  )
)

lazy val akane = (project in file("."))
  .settings(akaneSettings)
  .aggregate(ioc, legacy, knp, util, macros, knpAkka)

lazy val ioc = akaneProject("ioc", file("ioc"))

lazy val legacy = akaneProject("legacy", file("legacy"))
  .settings(Seq(
    libraryDependencies ++= akaneDeps
  ) ++ commonDeps)
  .dependsOn(util, knpAkka)

lazy val knp = akaneProject("knp", file("knp"))
  .settings(pbScala())
  .settings(
    libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.4"
  )
  .dependsOn(util, macros % Provided)

lazy val util = akaneProject("util", file("util"))
    .settings(
      libraryDependencies ++= Seq(
        "org.apache.commons" % "commons-lang3" % "3.3.2",
        "commons-io" % "commons-io" % "2.4",
        "com.typesafe" % "config" % "1.3.0"
      )
    )

lazy val macros = akaneProject("macros", file("macros"))
  .settings(macroDeps)

lazy val knpAkka = akaneProject("knp-akka", file("knp-akka"))
  .settings(akkaDeps)
  .dependsOn(knp)
