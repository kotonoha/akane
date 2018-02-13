val scalaPbVersion = "0.6.7"

addSbtPlugin("com.thesamet" % "sbt-protoc" % "0.99.14")

libraryDependencies += "com.trueaccord.scalapb" %% "compilerplugin" % scalaPbVersion
