organization := "com.spotify.data"
name := "gcs-tools"
version := "0.1.8-SNAPSHOT"

val gcsVersion = "1.6.3-hadoop2"
val hadoopVersion = "2.7.4"
val avroVersion = "1.8.2"
val parquetVersion = "1.10.1"
val protobufVersion = "3.4.0"
val protobufGenericVersion = "0.2.4"

val commonSettings = assemblySettings ++ Seq(
  scalaVersion := "2.12.8",
  autoScalaLibrary := false
)

lazy val root  = (
  project in file (".")
).aggregate(
  avroTools,
  parquetTools,
  protoTools
)

lazy val shared = (
  project in file("shared")
).settings(
  commonSettings
)

lazy val avroTools = (
  project in file ("avro-tools")
).settings(
  commonSettings ++ Seq(
    mainClass in assembly := Some("org.apache.avro.tool.Main"),
    assemblyJarName in assembly := s"avro-tools-$avroVersion.jar",
    libraryDependencies ++= Seq(
      "org.apache.avro" % "avro-tools" % avroVersion,
      "com.google.cloud.bigdataoss" % "gcs-connector" % gcsVersion
    )
  )
).dependsOn(
  shared
)

lazy val parquetTools = (
  project in file ("parquet-tools")
).settings(
  commonSettings ++ Seq(
    mainClass in assembly := Some("org.apache.parquet.tools.Main"),
    assemblyJarName in assembly := s"parquet-tools-$parquetVersion.jar",
    libraryDependencies ++= Seq(
      "org.apache.parquet" % "parquet-tools" % parquetVersion,
      "org.apache.hadoop" % "hadoop-common" % hadoopVersion,
      "org.apache.hadoop" % "hadoop-client" % hadoopVersion,
      "com.google.cloud.bigdataoss" % "gcs-connector" % gcsVersion
    )
  )
).dependsOn(
  shared
)

lazy val protoTools = (
  project in file("proto-tools")
).settings(
  commonSettings ++ Seq(
    mainClass in assembly := Some("org.apache.avro.tool.ProtoMain"),
    assemblyJarName in assembly := s"proto-tools-$protobufVersion.jar",
    assemblyShadeRules in assembly := Seq(
      ShadeRule.zap("com.google.protobuf.**").inLibrary("org.apache.avro" % "avro-tools" % avroVersion)
    ),
    libraryDependencies ++= Seq(
      "me.lyh" %% "protobuf-generic" % protobufGenericVersion exclude("com.google.guava", "guava"),
      "com.google.protobuf" % "protobuf-java" % protobufVersion,
      "org.apache.avro" % "avro-tools" % avroVersion,
      "com.google.cloud.bigdataoss" % "gcs-connector" % gcsVersion
    )
  )
).dependsOn(
  shared
)

lazy val assemblySettings = Seq(
  assemblyMergeStrategy in assembly ~= (old => {
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
    case PathList("META-INF", "NOTICE") => MergeStrategy.rename
    case _ => MergeStrategy.last
  })
)
