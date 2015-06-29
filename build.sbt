import sbt._

organization := "ws.kotonoha"

scalaVersion := "2.11.7"

name := "Akane"

version := "0.2-SNAPSHOT"

moduleName := "akane"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.1" % "test"

resolvers += JavaNet2Repository

libraryDependencies ++= Seq(          
            "net.liftweb" %% "lift-json" % "2.6-RC1",
            "javax.transaction" % "jta" % "1.0.1B" % "provided"
          )

libraryDependencies += "com.github.scala-incubator.io" %% "scala-io-core" % "0.4.3"

libraryDependencies +=  "com.github.scala-incubator.io" %% "scala-io-file" % "0.4.3"

libraryDependencies += "io.netty" % "netty" % "3.9.2.Final"

resolvers += Classpaths.typesafeReleases

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.3.4"

resolvers ++= Seq(
    "jboss repo" at "http://repository.jboss.org/nexus/content/groups/public-jboss/"
)

libraryDependencies += "org.apache.commons" % "commons-lang3" % "3.3.2"

libraryDependencies += "commons-io" % "commons-io" % "2.4"

libraryDependencies += "com.nativelibs4java" % "bridj" % "0.6.2"

libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.0.0"

javacOptions ++= Seq("-encoding", "utf8")

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.2"
