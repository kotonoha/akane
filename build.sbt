import sbt._

organization := "ws.kotonoha"

name := "Akane"

version := "0.1-SNAPSHOT"

moduleName := "akane"

libraryDependencies += "org.scalatest" %% "scalatest" % "1.7.2" % "test"

libraryDependencies ++=
					Seq("org.scalaz" %% "scalaz-core" % "6.0.3",
						"com.github.jsuereth.scala-arm" %% "scala-arm" % "1.0"
					)

libraryDependencies += "com.github.scala-incubator.io" %% "scala-io-core" % "0.2.0"

libraryDependencies +=  "com.github.scala-incubator.io" %% "scala-io-file" % "0.2.0"

libraryDependencies ++= Seq("org.jboss.netty" % "netty" % "3.2.7.Final")

 
libraryDependencies += "com.typesafe.akka" % "akka-actor" % "2.0.2"

resolvers ++= Seq("jboss repo" at "http://repository.jboss.org/nexus/content/groups/public-jboss/")

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
