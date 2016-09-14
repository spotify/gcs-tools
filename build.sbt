organization := "sh.rav.data"
name := "avro-z"
version := "0.1.0-SNAPSHOT"

scalaVersion := "2.11.8"
autoScalaLibrary := false
javacOptions ++= Seq("-source", "1.7", "-target", "1.7")
scalacOptions += "-target:jvm-1.7"

mainClass in Compile := Some("org.apache.avro.tool.Main")

libraryDependencies ++= Seq(
  "org.apache.avro" % "avro-tools" % "1.7.7",
  "com.google.cloud.bigdataoss" % "gcs-connector" % "1.5.2-hadoop2"
)

mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) => {
  case PathList("VERSION.txt") => MergeStrategy.rename
  case s => old(s)
}}
