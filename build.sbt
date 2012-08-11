import sbt._

organization := "ru.dijkstra"

name := "Ranobe Parser"

version := "0.1-SNAPSHOT"

moduleName := "ranobe-parser"

libraryDependencies += "org.scalatest" %% "scalatest" % "1.7.2" % "test"

libraryDependencies ++=
					Seq("org.scalaz" %% "scalaz-core" % "6.0.3",
						"com.github.jsuereth.scala-arm" %% "scala-arm" % "1.0"
					)

libraryDependencies += "com.github.scala-incubator.io" %% "scala-io-core" % "0.2.0"

libraryDependencies +=  "com.github.scala-incubator.io" %% "scala-io-file" % "0.2.0"
