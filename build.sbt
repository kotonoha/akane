import sbt._

organization := "ws.kotonoha"

scalaVersion := "2.9.2"

name := "Akane"

version := "0.1-SNAPSHOT"

moduleName := "akane"

libraryDependencies += "org.scalatest" %% "scalatest" % "1.8" % "test"

libraryDependencies ++=
					Seq("org.scalaz" %% "scalaz-core" % "6.0.4",
						"com.jsuereth" %% "scala-arm" % "1.2",
						"javax.transaction" % "jta" % "1.0.1B" % "provided"
					)

libraryDependencies += "com.github.scala-incubator.io" %% "scala-io-core" % "0.4.1"

libraryDependencies +=  "com.github.scala-incubator.io" %% "scala-io-file" % "0.4.1"

libraryDependencies ++= Seq("org.jboss.netty" % "netty" % "3.2.7.Final")
 
libraryDependencies += "com.typesafe.akka" % "akka-actor" % "2.0.2"

resolvers ++= Seq("jboss repo" at "http://repository.jboss.org/nexus/content/groups/public-jboss/")

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
