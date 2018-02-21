val scalaPbVersion = "0.7.0"

addSbtPlugin("com.thesamet" % "sbt-protoc" % "0.99.15")

libraryDependencies += "com.trueaccord.scalapb" %% "compilerplugin" % scalaPbVersion
