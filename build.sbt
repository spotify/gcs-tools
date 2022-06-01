import ReleaseTransformations._

organization := "com.spotify.data"
name := "gcs-tools"

val gcsVersion = "hadoop3-2.1.3"
val hadoopVersion = "3.3.0"
val joptVersion = "5.0.4"
val avroVersion = "1.11.0"
val magnolifyVersion = "0.4.3"
val parquetVersion = "1.11.1"
val protobufVersion = "3.15.5"
val protobufGenericVersion = "0.2.9"
val commonsLangVersion = "2.6"

val commonSettings = assemblySettings ++ Seq(
  scalaVersion := "2.13.5",
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
    parquetCli,
    protoTools,
    magnolifyTools
  )

lazy val shared = project
  .in(file("shared"))
  .settings(commonSettings)

lazy val avroTools = project
  .in(file("avro-tools"))
  .settings(commonSettings)
  .settings(
    assembly / mainClass := Some("org.apache.avro.tool.Main"),
    assembly / assemblyJarName := s"avro-tools-$avroVersion.jar",
    libraryDependencies ++= Seq(
      "org.apache.avro" % "avro-tools" % avroVersion,
      "org.apache.hadoop" % "hadoop-common" % hadoopVersion,
      "org.apache.hadoop" % "hadoop-client" % hadoopVersion,
      "com.google.cloud.bigdataoss" % "gcs-connector" % gcsVersion
    )
  )
  .dependsOn(shared)

lazy val parquetCli = project
  .in(file("parquet-cli"))
  .settings(commonSettings)
  .settings(
    assembly / mainClass := Some("org.apache.parquet.cli.Main"),
    assembly / assemblyJarName := s"parquet-cli-$parquetVersion.jar",
    libraryDependencies ++= Seq(
      "org.apache.parquet" % "parquet-cli" % parquetVersion,
      "org.apache.hadoop" % "hadoop-common" % hadoopVersion,
      "org.apache.hadoop" % "hadoop-client" % hadoopVersion,
      "com.google.cloud.bigdataoss" % "gcs-connector" % gcsVersion,
      // Broken transitive from hadoop-common, fixed in 1.12.0 (PARQUET-1844)
      "commons-lang" % "commons-lang" % commonsLangVersion
    )
  )
  .dependsOn(shared)

lazy val protoTools = project
  .in(file("proto-tools"))
  .settings(commonSettings)
  .settings(
    assembly / mainClass := Some("org.apache.avro.tool.ProtoMain"),
    assembly / assemblyJarName := s"proto-tools-$protobufVersion.jar",
    libraryDependencies ++= Seq(
      "me.lyh" %% "protobuf-generic" % protobufGenericVersion,
      "net.sf.jopt-simple" % "jopt-simple" % joptVersion,
      "com.google.protobuf" % "protobuf-java" % protobufVersion,
      "org.apache.avro" % "avro-mapred" % avroVersion,
      "org.apache.hadoop" % "hadoop-common" % hadoopVersion,
      "org.apache.hadoop" % "hadoop-client" % hadoopVersion,
      "com.google.cloud.bigdataoss" % "gcs-connector" % gcsVersion
    )
  )
  .dependsOn(shared)

lazy val magnolifyTools = project
  .in(file("magnolify-tools"))
  .settings(commonSettings)
  .settings(
    assembly / mainClass := Some("magnolify.tools.Main"),
    assembly / assemblyJarName := s"magnolify-tools-$magnolifyVersion.jar",
    libraryDependencies ++= Seq(
      "net.sf.jopt-simple" % "jopt-simple" % joptVersion,
      "org.apache.avro" % "avro" % avroVersion,
      "org.apache.parquet" % "parquet-hadoop" % parquetVersion,
      "org.apache.hadoop" % "hadoop-common" % hadoopVersion,
      "org.apache.hadoop" % "hadoop-client" % hadoopVersion,
      "com.google.cloud.bigdataoss" % "gcs-connector" % gcsVersion,
      "com.spotify" %% "magnolify-tools" % magnolifyVersion
    ),
    dependencyOverrides ++= Seq(
      "com.google.guava" % "guava" % "29.0-jre"
    )
  )
  .dependsOn(shared)

lazy val assemblySettings = Seq(
  assembly / assemblyMergeStrategy ~= (old => {
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
