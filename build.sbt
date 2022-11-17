import ReleaseTransformations._

organization := "com.spotify.data"
name := "gcs-tools"

val gcsVersion = "hadoop3-2.2.9"
val guavaVersion = "31.1-jre" // otherwise android is taken
val hadoopVersion = "3.3.3"
val joptVersion = "5.0.4"
val avroVersion = "1.11.0"
val magnolifyVersion = "0.6.2"
val parquetVersion = "1.12.3"
val protobufVersion = "3.21.1"
val protobufGenericVersion = "0.2.9"
val commonsLangVersion = "2.6"

val commonSettings = assemblySettings ++ Seq(
  crossPaths := false,
  autoScalaLibrary := false,
  javacOptions ++= Seq("--release", "8")
)

lazy val root = project
  .in(file("."))
  .settings(commonSettings)
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

def exclude(moduleNames: String*)(
    dependencies: Vector[Assembly.Dependency]
): Either[String, Vector[Assembly.JarEntry]] = {
  val filtered = if (dependencies.size > 1) {
    dependencies.filter {
      case l: Assembly.Library => !moduleNames.contains(l.moduleCoord.name)
      case _                   => true
    }
  } else {
    dependencies
  }

  if (filtered.nonEmpty) {
    MergeStrategy.deduplicate.apply(filtered)
  } else {
    Right(Vector.empty)
  }
}

def preserveName(moduleName: String)(d: Assembly.Dependency): String = d match {
  case l: Assembly.Library if l.moduleCoord.name == moduleName =>
    l.target
  case l: Assembly.Library =>
    l.target + "_" + l.moduleCoord.name + "-" + l.moduleCoord.version
  case p: Assembly.Project =>
    p.target + "_" + p.name
}

lazy val discardMetaFiles = Set(
  "DEPENDENCIES",
  "MANIFEST.MF",
  "LICENSE",
  "LICENSE.txt",
  "LICENSE.md",
  "INDEX.LIST",
  "NOTICE.md"
)

ThisBuild / dependencyOverrides ++= Seq(
  "com.google.guava" % "guava" % guavaVersion
)

lazy val signedMetaExtensions = Set(".DSA", ".RSA", ".SF")

def discardMeta(f: String): Boolean = {
  discardMetaFiles.contains(f) || signedMetaExtensions.exists(f.endsWith)
}

lazy val assemblySettings = Seq(
  assembly / assemblyMergeStrategy := {
    case PathList("javax", "xml", "bind", _*) =>
      // prefer jakarta over jaxb
      CustomMergeStrategy("xml")(exclude("jaxb-api", "avro-tools"))
    case PathList("javax", "ws", "rs", _*) =>
      // prefer rs-api over jsr311-api
      CustomMergeStrategy("rs")(exclude("jsr311-api", "avro-tools"))
    case PathList("org", "apache", "log4j", _*) =>
      // prefer reload4j over log4j
      CustomMergeStrategy("log4j")(exclude("log4j", "avro-tools"))
    case PathList("org", "slf4j", "impl", _*) =>
      // prefer slf4j-reload4j over slf4j-log4j12
      CustomMergeStrategy("slf4j-log4j")(exclude("slf4j-log4j12", "avro-tools"))
    case "log4j.properties" =>
      MergeStrategy.preferProject
    case x if x.endsWith(".properties") =>
      MergeStrategy.filterDistinctLines
    case PathList("META-INF", "services", _*) =>
      MergeStrategy.filterDistinctLines
    case PathList("META-INF", "NOTICE") =>
      // avro-tools META-INF/NOTICE must not be renamed
      CustomMergeStrategy.rename(preserveName("avro-tools"))
    case PathList("META-INF", "NOTICE.txt") =>
      MergeStrategy.rename
    case PathList("NOTICE") =>
      MergeStrategy.rename
    case PathList("META-INF", "maven" | "versions", _*) =>
      MergeStrategy.discard
    case PathList("META-INF", x) if discardMeta(x) =>
      MergeStrategy.discard
    case PathList("module-info.class" | "LICENSE" | "rootdoc.txt") =>
      MergeStrategy.discard
    case "com/google/common/flogger/backend/system/DefaultPlatform.class" =>
      MergeStrategy.first
    case "META-INF/native-image/io.netty/transport/reflection-config.json" =>
      MergeStrategy.first
    case x =>
      // avro-tools is a fat jar
      // in case of conflict prefer library from other source
      CustomMergeStrategy("avro-tools")(exclude("avro-tools"))
  }
)
