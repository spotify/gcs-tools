import ReleaseTransformations._

organization := "com.spotify.data"
name := "gcs-tools"

val avroVersion = "1.11.1"
val commonsLangVersion = "2.6"
val gcsVersion = "hadoop3-2.2.12"
val guavaVersion = "31.1-jre" // otherwise android is taken
val hadoopVersion = "3.3.5"
val jacksonVersion = "2.15.0"
val joptVersion = "5.0.4"
val magnolifyVersion = "0.6.2"
val parquetVersion = "1.13.0"
val protobufGenericVersion = "0.2.9"
val protobufVersion = "3.22.3"
val scalatestVersion = "3.2.15"

ThisBuild / PB.protocVersion := protobufVersion
lazy val protobufConfigSettings = Def.settings(
  PB.targets := Seq(
    PB.gens.java -> (ThisScope.copy(config = Zero) / sourceManaged).value /
      "compiled_proto" /
      configuration.value.name,
  ),
  managedSourceDirectories ++= PB.targets.value.map(_.outputPath)
)
lazy val protobufSettings = Seq(Compile, Test)
  .flatMap(c => inConfig(c)(protobufConfigSettings))

val commonSettings = assemblySettings ++ Seq(
  scalaVersion := "2.13.10",
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
  .settings(protobufSettings)
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
      "com.google.cloud.bigdataoss" % "gcs-connector" % gcsVersion,
      "org.scalatest" %% "scalatest" % scalatestVersion % Test
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
      "com.spotify" %% "magnolify-tools" % magnolifyVersion,
      "org.scalatest" %% "scalatest" % scalatestVersion % Test,
      "org.apache.parquet" % "parquet-avro" % parquetVersion % Test,
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
  // force jre version
  "com.google.guava" % "guava" % guavaVersion,
  // sync all jackson versions
  "com.fasterxml.jackson.core" % "jackson-annotations" % jacksonVersion,
  "com.fasterxml.jackson.core" % "jackson-core" % jacksonVersion,
  "com.fasterxml.jackson.core" % "jackson-databind" % jacksonVersion,
  "com.fasterxml.jackson.jaxrs" % "jackson-jaxrs-base" % jacksonVersion,
  "com.fasterxml.jackson.jaxrs" % "jackson-jaxrs-json-provider" % jacksonVersion,
  "com.fasterxml.jackson.module" % "jackson-module-jaxb-annotations" % jacksonVersion,
  "com.fasterxml.jackson.module" % "jackson-module-paranamer" % jacksonVersion,
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % jacksonVersion,
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
