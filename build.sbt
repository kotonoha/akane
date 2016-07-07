lazy val akaneSettings = Seq(
  organization := "ws.kotonoha",
  moduleName := "akane",
  crossScalaVersions := Seq("2.11.8"),
  scalaVersion := "2.11.8",
  name := "Akane",
  version := "0.2-SNAPSHOT",

  javacOptions ++= Seq("-encoding", "utf8")
)

lazy val akaneDeps = Seq(
  //test
  "org.scalatest" %% "scalatest" % "2.2.1" % "test",

  "net.liftweb" %% "lift-json" % "2.6.3",
  "com.jsuereth" %% "scala-arm" % "1.4",

  "com.github.scala-incubator.io" %% "scala-io-core" % "0.4.3",
  "com.github.scala-incubator.io" %% "scala-io-file" % "0.4.3",

  "org.apache.commons" % "commons-lang3" % "3.3.2",
  "commons-io" % "commons-io" % "2.4",

  "com.nativelibs4java" % "bridj" % "0.7.0",

  "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
  "ch.qos.logback" % "logback-classic" % "1.1.7",

  "com.typesafe" % "config" % "1.3.0",
  "com.typesafe.akka" %% "akka-actor" % "2.4.6"
)

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

lazy val akane = (project in file("."))
  .settings(akaneSettings)
    .settings(Seq(
      libraryDependencies ++= akaneDeps
    ))
  .settings(pbScala())
