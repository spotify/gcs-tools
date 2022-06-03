import ReleaseTransformations._

organization := "com.spotify.data"
name := "gcs-tools"

val gcsVersion = "hadoop3-2.1.8"
val hadoopVersion = "3.3.3"
val joptVersion = "5.0.4"
val avroVersion = "1.11.0"
val magnolifyVersion = "0.4.8"
val parquetVersion = "1.12.3"
val protobufVersion = "3.21.1"
val protobufGenericVersion = "0.2.9"
val commonsLangVersion = "2.6"

val commonSettings = assemblySettings ++ Seq(
  scalaVersion := "2.13.8",
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
      "com.google.cloud.bigdataoss" % "gcs-connector" % gcsVersion
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

// avro-tools is a fat jar which includes old Guava & Hadoop classes
def dependencyFilter(
    conflicts: Vector[Assembly.Dependency]
): Either[String, Vector[Assembly.JarEntry]] = {
  val filtered = conflicts
    .filter {
      case l: Assembly.Library => l.moduleCoord.name != "avro-tools"
      case _                   => true
    }
    .map(f => JarEntry(f.target, f.stream))
  Right(filtered)
}

// avro-tools META-INF/NOTICE must not be renamed
def noticeRename(d: Assembly.Dependency): String = {
  case l: Assembly.Library if l.moduleCoord.name == "avro-tools" =>
    l.target
  case l: Assembly.Library =>
    l.target + "_" + l.moduleCoord.name + "-" + l.moduleCoord.version
  case p: Assembly.Project =>
    p.target + "_" + p.name
}

lazy val assemblySettings = Seq(
  assembly / assemblyMergeStrategy ~= (old => {
    case PathList("com", "google", "common", _*) =>
      CustomMergeStrategy("guava")(dependencyFilter)
    case PathList("com", "google", "protobuf", _*) =>
      CustomMergeStrategy("protobuf")(dependencyFilter)
    case PathList("org", "apache", "hadoop", _*) =>
      CustomMergeStrategy("hadoop")(dependencyFilter)
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
    case PathList("META-INF", "NOTICE") =>
      CustomMergeStrategy.rename(noticeRename)
    case PathList("META-INF", "services", "org.apache.hadoop.fs.FileSystem") =>
      MergeStrategy.filterDistinctLines
    case PathList("META-INF", "LICENSE")               => MergeStrategy.discard
    case PathList("META-INF", "MANIFEST.MF")           => MergeStrategy.discard
    case PathList("META-INF", "INDEX.LIST")            => MergeStrategy.discard
    case PathList("META-INF", s) if s.endsWith(".DSA") => MergeStrategy.discard
    case PathList("META-INF", s) if s.endsWith(".RSA") => MergeStrategy.discard
    case PathList("META-INF", s) if s.endsWith(".SF")  => MergeStrategy.discard
    case _                                             => MergeStrategy.last
  })
)
