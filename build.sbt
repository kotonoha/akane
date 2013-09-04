import sbt._

organization := "ws.kotonoha"

scalaVersion := "2.10.2"

name := "Akane"

version := "0.2-SNAPSHOT"

moduleName := "akane"

libraryDependencies += "org.scalatest" %% "scalatest" % "1.9.1" % "test"

libraryDependencies ++=	Seq(					
						"net.liftweb" %% "lift-json" % "2.5.1",
						"javax.transaction" % "jta" % "1.0.1B" % "provided"
					)

resolvers += "java.net" at "http://download.java.net/maven/2/"

libraryDependencies += "com.github.scala-incubator.io" %% "scala-io-core" % "0.4.2"

libraryDependencies +=  "com.github.scala-incubator.io" %% "scala-io-file" % "0.4.2"

libraryDependencies ++= Seq("org.jboss.netty" % "netty" % "3.2.7.Final")
 
resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.2.0"

resolvers ++= Seq("jboss repo" at "http://repository.jboss.org/nexus/content/groups/public-jboss/")

libraryDependencies += "org.apache.commons" % "commons-lang3" % "3.1"

libraryDependencies += "commons-io" % "commons-io" % "2.4"

resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/groups/public"

libraryDependencies += "com.nativelibs4java" % "bridj" % "0.6.2"

libraryDependencies += "com.typesafe" %% "scalalogging-slf4j" % "1.0.1"

javacOptions ++= Seq("-encoding", "utf8")

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.0.13"
