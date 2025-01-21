addDependencyTreePlugin
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.4")
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "2.3.1")
addSbtPlugin("com.github.sbt" % "sbt-release" % "1.4.0")
addSbtPlugin("com.github.sbt" % "sbt-avro" % "4.0.0")
addSbtPlugin("com.thesamet" % "sbt-protoc" % "1.0.7")

libraryDependencies += "org.apache.avro" % "avro-compiler" % "1.12.0"
