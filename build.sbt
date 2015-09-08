import sbt._

organization := "ws.kotonoha"

crossScalaVersions := Seq("2.11.7")

// scalaVersion := "2.11.7"

name := "Akane"

version := "0.2-SNAPSHOT"

moduleName := "akane"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.1" % "test"

resolvers += JavaNet2Repository

libraryDependencies ++= Seq(
            "net.liftweb" %% "lift-json" % "2.6-RC1",
            "com.jsuereth" %% "scala-arm" % "1.4"
          )

libraryDependencies += "com.github.scala-incubator.io" %% "scala-io-core" % "0.4.3"

libraryDependencies +=  "com.github.scala-incubator.io" %% "scala-io-file" % "0.4.3"

resolvers += Classpaths.typesafeReleases

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.4.0-RC2"

libraryDependencies += "com.typesafe" % "config" % "1.3.0"

resolvers ++= Seq(
    "jboss repo" at "http://repository.jboss.org/nexus/content/groups/public-jboss/"
)

libraryDependencies += "org.apache.commons" % "commons-lang3" % "3.3.2"

libraryDependencies += "commons-io" % "commons-io" % "2.4"

libraryDependencies += "com.nativelibs4java" % "bridj" % "0.7.0"

libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0"

javacOptions ++= Seq("-encoding", "utf8")

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.2"
