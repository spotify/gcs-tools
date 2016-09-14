organization := "com.spotify.data"
name := "avro-z"
version := "0.1.0-SNAPSHOT"

scalaVersion := "2.11.8"
autoScalaLibrary := false
javacOptions ++= Seq("-source", "1.7", "-target", "1.7")
scalacOptions += "-target:jvm-1.7"

mainClass in Compile := Some("org.apache.avro.tool.Main")

libraryDependencies ++= Seq(
  //"org.apache.hadoop" % "hadoop-client" % "2.6.0.2.2.6.0-2800",
  "org.apache.avro" % "avro-tools" % "1.7.7",
  "com.google.cloud.bigdataoss" % "gcs-connector" % "1.5.2-hadoop2"
)

mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) => {
  case s if s.endsWith(".properties") => MergeStrategy.filterDistinctLines
  case s if s.endsWith("pom.xml") => MergeStrategy.last
  case s if s.endsWith(".class") => MergeStrategy.last
  case s if s.endsWith("libjansi.jnilib") => MergeStrategy.last
  case s if s.endsWith("jansi.dll") => MergeStrategy.rename
  case s if s.endsWith("libjansi.so") => MergeStrategy.rename
  case s if s.endsWith("libsnappyjava.jnilib") => MergeStrategy.last
  case s if s.endsWith("libsnappyjava.so") => MergeStrategy.last
  case s if s.endsWith("snappyjava_snappy.dll") => MergeStrategy.last
  case s if s.endsWith(".dtd") => MergeStrategy.rename
  case s if s.endsWith(".xsd") => MergeStrategy.rename
  case PathList("META-INF", "services", "org.apache.hadoop.fs.FileSystem") => MergeStrategy.filterDistinctLines
  case PathList("META-INF", "LICENSE") => MergeStrategy.discard
  case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
  case PathList("META-INF", "DUMMY.SF") => MergeStrategy.discard
  case PathList("META-INF", "DUMMY.RSA") => MergeStrategy.discard
  case PathList("META-INF", "DUMMY.DSA") => MergeStrategy.discard
  case PathList("META-INF", "MSFTSIG.RSA") => MergeStrategy.discard
  case PathList("META-INF", "MSFTSIG.SF") => MergeStrategy.discard
  case _ => MergeStrategy.last
}}
