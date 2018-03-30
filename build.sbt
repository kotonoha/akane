import sbt.internal.LoadedBuild

lazy val scalaPbVersion = "0.7.0"

def pbScala(): Seq[Setting[_]] = {
  Def.settings(
    Compile / PB.targets := Seq(
      scalapb.gen(flatPackage = true, grpc = true) -> (sourceManaged in Compile).value
    ),
    libraryDependencies ++= Seq(
      "com.thesamet.scalapb" %% "scalapb-runtime" % scalaPbVersion % "protobuf"
    )
  )
}

lazy val akaneSettings = Def.settings(
  organization := "ws.kotonoha",
  moduleName := "akane",
  crossScalaVersions := Seq("2.11.12", "2.12.4"),
  name := "Akane",
  version := "0.2-SNAPSHOT",
  javacOptions ++= Seq("-encoding", "utf8"),
  scalacOptions ++= Seq(
    "-feature",
    "-deprecation"
  ),
  scalacOptions ++= (scalaBinaryVersion.value match {
    case "2.11" => Seq(
      "-Ybackend:GenBCode",
      "-Yopt:l:classpath",
      "-Yopt-warnings",
      "-target:jvm-1.8"
    )
    case "2.12" => Seq(
      "-opt:l:inline",
      "-opt-inline-from:**",
      "-opt-warnings:at-inline-failed"
    )
    case v => throw new Exception(s"Unsuported version, $v")
  }),
  libraryDependencies ++= (scalaBinaryVersion.value match {
    case "2.11" => Seq(
      "org.scala-lang.modules" % "scala-java8-compat_2.11" % "0.8.0"
    )
    case _ => Nil
  })
)


def akaneProject(projName: String, basePath: File) = {
  val id = s"akane-$projName"

  val localSettings = Def.settings(
    name := s"Akane ${projName.capitalize}",
    moduleName := id,
    libraryDependencies += "com.google.code.findbugs" % "jsr305" % "3.0.0" % Provided
  )

  Project(id = id, base = basePath)
    .settings(
      akaneSettings,
      localSettings,
      commonDeps
    )
}

val luceneVersion = "6.2.0"


lazy val akkaDep = "com.typesafe.akka" %% "akka-actor" % "2.5.9"


lazy val scalatest = "org.scalatest" %% "scalatest" % "3.0.5"
lazy val scalacheck = "org.scalacheck" %% "scalacheck" % "1.13.5"
lazy val scalamock = "org.scalamock" %% "scalamock-scalatest-support" % "3.6.0"

lazy val commonDeps = Def.settings(
  libraryDependencies ++= Seq(
    "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2",
    scalatest % Test, scalamock % Test, scalacheck % Test,
    "ch.qos.logback" % "logback-classic" % "1.2.3" % Test
  )
)

lazy val akkaDeps = Def.settings(
  libraryDependencies += akkaDep
)

lazy val macroDeps = Def.settings(
  libraryDependencies ++= Seq(
    "org.scala-lang" % "scala-reflect" % scalaVersion.value,
    "org.scalamacros" %% "resetallattrs" % "1.0.0"
  )
)

lazy val luceneDeps = Def.settings(
  libraryDependencies ++= Seq(
    "org.apache.lucene" % "lucene-core" % luceneVersion,
    "org.apache.lucene" % "lucene-analyzers-common" % luceneVersion
  )
)

def isRoot(bld: LoadedBuild, proj: ResolvedProject): Boolean = {
  val refUri = proj.base.toURI
  val rootUri = bld.root
  val areEqual = rootUri.getScheme == "file" && rootUri == refUri
  areEqual
}

lazy val akane = (project in file("."))
  .settings(akaneSettings)
  .aggregate(ioc, legacy, knp, util, macros, knpAkka, blobdb, akka, dic, kytea, misc, `jmdict-lucene`, jumanppGrpc)
  .settings(
    publishArtifact := false,
    (scalaVersion in ThisBuild) := (if (isRoot(loadedBuild.value, thisProject.value)) "2.11.12" else scalaVersion.value)
  )

lazy val ioc = akaneProject("ioc", file("ioc"))

lazy val legacy = akaneProject("legacy", file("legacy"))
  .settings(
    commonDeps,
    libraryDependencies ++= Seq(
      akkaDep,
      "com.nativelibs4java" % "bridj" % "0.7.0",
      "com.typesafe" % "config" % "1.3.2",
      "org.scala-lang.modules" %% "scala-xml" % "1.0.6"
      // "com.jsuereth" %% "scala-arm" % "2.0",
    ),
    libraryDependencies ++= (scalaBinaryVersion.value match { // 2.11-stuff only
      case "2.11" => Seq(
        "com.github.scala-incubator.io" %% "scala-io-core" % "0.4.3",
        "com.github.scala-incubator.io" %% "scala-io-file" % "0.4.3"
      )
      case _ => Nil
    }),
    Compile / sourceDirectory := (scalaBinaryVersion.value match {
      case "2.11" => (Compile / sourceDirectory).value
      case "2.12" => file("fake-source-directory/fake/fake")
    }),
    Test / sourceDirectory := (scalaBinaryVersion.value match {
      case "2.11" => (Test / sourceDirectory).value
      case "2.12" => file("fake-source-directory/fake/fake")
    }),
    resolvers += "kyouni" at "http://lotus.kuee.kyoto-u.ac.jp/nexus/content/groups/public/",
    Test / fork := true
  )
  .dependsOn(util, knpAkka, kytea)

lazy val knp = akaneProject("knp", file("knp"))
  .settings(pbScala())
  .settings(
    libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.0"
  )
  .dependsOn(util, macros % Provided)

lazy val util = akaneProject("util", file("util"))
  .settings(
    libraryDependencies ++= Seq(
      "org.apache.commons" % "commons-lang3" % "3.3.2",
      "commons-io" % "commons-io" % "2.4",
      "com.typesafe" % "config" % "1.3.2"
    )
  )

lazy val macros = akaneProject("macros", file("macros"))
  .settings(macroDeps)

lazy val knpAkka = akaneProject("knp-akka", file("knp-akka"))
  .settings(akkaDeps)
  .dependsOn(knp, akka, testkit % Test)

lazy val jumanppGrpc = akaneProject("jumanpp-grpc", file("jumanpp-grpc"))
    .settings(pbScala())
    .settings(
      libraryDependencies ++= Seq(
        "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalaPbVersion
      )
    )
    .dependsOn(knp, testkit % Test)

lazy val blobdb = akaneProject("blobdb", file("blobdb"))
  .dependsOn(util)
  .settings(
    libraryDependencies ++= Seq(
      "org.mapdb" % "mapdb" % "1.0.9",
      "com.github.ben-manes.caffeine" % "caffeine" % "2.6.1",
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
  .settings(pbScala())
  .dependsOn(util)

lazy val misc = akaneProject("misc", file("misc"))
  .dependsOn(util)

lazy val `jmdict-lucene` = akaneProject("jmdict-lucene", file("dic/jmdict-lucene"))
  .dependsOn(dic)
  .settings(luceneDeps,
    libraryDependencies ++= Seq(
      "com.github.ben-manes.caffeine" % "caffeine" % "2.6.1",
      "joda-time" % "joda-time" % "2.9.9",
      "org.joda" % "joda-convert" % "1.9.2" % Optional
    )
  )


