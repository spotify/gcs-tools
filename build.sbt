import ReleaseTransformations._

organization := "com.spotify.data"
name := "gcs-tools"

val gcsVersion = "hadoop3-2.1.3"
val hadoopVersion = "3.2.1"
val avroVersion = "1.10.0"
val parquetVersion = "1.11.1"
val protobufVersion = "3.12.4"
val protobufGenericVersion = "0.2.9"

val commonSettings = assemblySettings ++ Seq(
  scalaVersion := "2.13.3",
  autoScalaLibrary := false,
  javacOptions ++= Seq("-source", "1.8", "-target", "1.8")
)

lazy val root = project
  .in(file("."))
  .settings(
    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      runClean,
      runTest,
      setReleaseVersion,
      commitReleaseVersion,
      tagRelease,
      setNextVersion,
      commitNextVersion,
      pushChanges
    )
  )
  .aggregate(
    avroTools,
    parquetTools,
    protoTools
  )

lazy val shared = project
  .in(file("shared"))
  .settings(commonSettings)

lazy val avroTools = project
  .in(file("avro-tools"))
  .settings(commonSettings)
  .settings(
    mainClass in assembly := Some("org.apache.avro.tool.Main"),
    assemblyJarName in assembly := s"avro-tools-$avroVersion.jar",
    libraryDependencies ++= Seq(
      "org.apache.avro" % "avro-tools" % avroVersion,
      "org.apache.hadoop" % "hadoop-common" % hadoopVersion,
      "org.apache.hadoop" % "hadoop-client" % hadoopVersion,
      "com.google.cloud.bigdataoss" % "gcs-connector" % gcsVersion
    )
  )
  .dependsOn(shared)

lazy val parquetTools = project
  .in(file("parquet-tools"))
  .settings(commonSettings)
  .settings(
    mainClass in assembly := Some("org.apache.parquet.tools.Main"),
    assemblyJarName in assembly := s"parquet-tools-$parquetVersion.jar",
    libraryDependencies ++= Seq(
      "org.apache.parquet" % "parquet-tools" % parquetVersion,
      "org.apache.hadoop" % "hadoop-common" % hadoopVersion,
      "org.apache.hadoop" % "hadoop-client" % hadoopVersion,
      "com.google.cloud.bigdataoss" % "gcs-connector" % gcsVersion
    )
  )
  .dependsOn(shared)

lazy val protoTools = project
  .in(file("proto-tools"))
  .settings(commonSettings)
  .settings(
    mainClass in assembly := Some("org.apache.avro.tool.ProtoMain"),
    assemblyJarName in assembly := s"proto-tools-$protobufVersion.jar",
    assemblyShadeRules in assembly := Seq(
      ShadeRule
        .zap("com.google.protobuf.**")
        .inLibrary("org.apache.avro" % "avro-tools" % avroVersion)
    ),
    libraryDependencies ++= Seq(
      "me.lyh" %% "protobuf-generic" % protobufGenericVersion exclude ("com.google.guava", "guava"),
      "com.google.protobuf" % "protobuf-java" % protobufVersion,
      "org.apache.avro" % "avro-tools" % avroVersion,
      "org.apache.hadoop" % "hadoop-common" % hadoopVersion,
      "org.apache.hadoop" % "hadoop-client" % hadoopVersion,
      "com.google.cloud.bigdataoss" % "gcs-connector" % gcsVersion
    )
  )
  .dependsOn(shared)

lazy val assemblySettings = Seq(
  assemblyMergeStrategy in assembly ~= (old => {
    // avro-tools is a fat jar which includes old Guava & Hadoop classes
    case PathList("com", "google", "common", _*)  =>
      jarFilter("guava")(_.toString.contains("/com/google/guava/guava"))
    case PathList("com", "google", "protobuf", _*)  =>
      jarFilter("protobuf")(_.toString.contains("/com/google/protobuf/protobuf-java"))
    case PathList("org", "apache", "hadoop", _*)  =>
      jarFilter("hadoop")(_.toString.contains("/org/apache/hadoop/hadoop"))
    case s if s.endsWith(".properties")           => MergeStrategy.filterDistinctLines
    case s if s.endsWith("pom.xml")               => MergeStrategy.last
    case s if s.endsWith(".class")                => MergeStrategy.last
    case s if s.endsWith("libjansi.jnilib")       => MergeStrategy.last
    case s if s.endsWith("jansi.dll")             => MergeStrategy.rename
    case s if s.endsWith("libjansi.so")           => MergeStrategy.rename
    case s if s.endsWith("libsnappyjava.jnilib")  => MergeStrategy.last
    case s if s.endsWith("libsnappyjava.so")      => MergeStrategy.last
    case s if s.endsWith("snappyjava_snappy.dll") => MergeStrategy.last
    case s if s.endsWith(".dtd")                  => MergeStrategy.rename
    case s if s.endsWith(".xsd")                  => MergeStrategy.rename
    case PathList("META-INF", "services", "org.apache.hadoop.fs.FileSystem") =>
      MergeStrategy.filterDistinctLines
    case PathList("META-INF", "LICENSE")     => MergeStrategy.discard
    case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
    case PathList("META-INF", "INDEX.LIST")  => MergeStrategy.discard
    case PathList("META-INF", s) if s.endsWith(".DSA") => MergeStrategy.discard
    case PathList("META-INF", s) if s.endsWith(".RSA") => MergeStrategy.discard
    case PathList("META-INF", s) if s.endsWith(".SF")  => MergeStrategy.discard
    case PathList("META-INF", "NOTICE")      => MergeStrategy.rename
    case _                                   => MergeStrategy.last
  })
)

import sbtassembly.AssemblyUtils
import sbtassembly.MergeStrategy

def jarFilter(_name: String)(f: File => Boolean): MergeStrategy = new MergeStrategy {
  override def name = _name

  override def apply(tempDir: File, path: String, files: Seq[File]): Either[String, Seq[(File, String)]] = {
    val filtered = files
      .map(f => f -> AssemblyUtils.sourceOfFileForMerge(tempDir, f))
      .filter { case (_, (jar, _, _, isJar)) =>
        isJar && f(jar)
      }
    val pick = if (filtered.isEmpty) files.last else filtered.last._1
    Right(Seq(pick -> path))
  }
}
