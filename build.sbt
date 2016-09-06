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

  javacOptions ++= Seq("-encoding", "utf8"),

  scalacOptions ++= Seq(
    "-Ybackend:GenBCode",
    "-Yopt:l:classpath",
    "-Yopt-warnings",
    "-target:jvm-1.8",
    "-feature",
    "-deprecation"
  ),
  scalacOptions in Compile ++= (if (scalaVersion.value.startsWith("2.11.8")) {
    Seq("-Ydelambdafy:method")
  } else {
    Seq.empty
  }),
  scalacOptions in Test ++= (if (scalaVersion.value.startsWith("2.11.8")) {
    Seq("-Ydelambdafy:inline")
  } else {
    Seq.empty
  }),
  libraryDependencies ++= Seq("org.scala-lang.modules" % "scala-java8-compat_2.11" % "0.7.0")

)


def akaneProject(projName: String, basePath: File) = {
  val id = s"akane-$projName"

  val localSettings = Def.settings(
    name := s"Akane ${projName.capitalize}",
    moduleName := id,
    libraryDependencies += "com.google.code.findbugs" % "jsr305" % "3.0.0" % Provided
  )

  val allSettings = akaneSettings ++ localSettings ++ commonDeps

  Project(id = id, base = basePath, settings = allSettings)
}

lazy val akkaDep = "com.typesafe.akka" %% "akka-actor" % "2.4.9"


lazy val akaneDeps = Seq(
  //test

  "net.liftweb" %% "lift-json" % "3.0-RC3",
  "com.jsuereth" %% "scala-arm" % "1.4",

  "com.github.scala-incubator.io" %% "scala-io-core" % "0.4.3",
  "com.github.scala-incubator.io" %% "scala-io-file" % "0.4.3",

  "com.nativelibs4java" % "bridj" % "0.7.0",

  "com.typesafe" % "config" % "1.3.0",
  akkaDep
)

lazy val scalatest = "org.scalatest" %% "scalatest" % "2.2.6"
lazy val scalacheck = "org.scalacheck" %% "scalacheck" % "1.12.5"
lazy val scalamock = "org.scalamock" %% "scalamock-scalatest-support" % "3.2.2"

lazy val commonDeps = Def.settings(
  libraryDependencies ++= Seq(
    "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
    scalatest % Test,
    "ch.qos.logback" % "logback-classic" % "1.1.7" % Test
  )
)

lazy val akkaDeps = Def.settings(
  libraryDependencies += akkaDep
)

lazy val macroDeps = Def.settings(
  libraryDependencies ++= Seq(
    "org.scala-lang" % "scala-reflect" % scalaVersion.value,
    "org.scalamacros" %% "resetallattrs" % "1.0.0-M1"
  )
)

lazy val akane = (project in file("."))
  .settings(akaneSettings)
  .aggregate(ioc, legacy, knp, util, macros, knpAkka, blobdb, akka, dic, kytea)

lazy val ioc = akaneProject("ioc", file("ioc"))

lazy val legacy = akaneProject("legacy", file("legacy"))
  .settings(Seq(
    libraryDependencies ++= akaneDeps,
    resolvers += "kyouni" at "http://lotus.kuee.kyoto-u.ac.jp/nexus/content/groups/public/"
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
  .dependsOn(knp, testkit % Test)

lazy val blobdb = akaneProject("blobdb", file("blobdb"))
  .dependsOn(util)
  .settings(
    libraryDependencies ++= Seq(
      "org.mapdb" % "mapdb" % "1.0.9",
      "com.github.ben-manes.caffeine" % "caffeine" % "2.3.2",
      "net.jpountz.lz4" % "lz4" % "1.3.0",
      akkaDep
    )
  )

lazy val testkit = akaneProject("testkit", file("testkit"))
  .dependsOn(knp)
  .settings(
    libraryDependencies ++= Seq(scalatest, scalamock, scalacheck)
  )

lazy val dic = akaneProject("dic", file("dic"))
  .settings(pbScala())
  .dependsOn(util)

lazy val akka = akaneProject("akka", file("akka"))
    .dependsOn(util)
    .settings(akkaDeps)

lazy val kytea = akaneProject("kytea", file("kytea"))
  .dependsOn(util)
