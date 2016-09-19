GCS Tools
=========

[![GitHub license](https://img.shields.io/github/license/spotify/gcs-tools.svg)](./LICENSE)

## Raison d'être:

Light weight wrapper that adds [Google Cloud
Storage](https://cloud.google.com/storage/) (GCS) support to common Hadoop
tools, including
[avro-tools](https://mvnrepository.com/artifact/org.apache.avro/avro-tools) and
[parquet-tools](https://mvnrepository.com/artifact/org.apache.parquet/parquet-tools),
so that they can be used from regular workstations or laptops, outside of a
[Google Compute Engine](https://cloud.google.com/compute/) (GCE) instance.

It uses your existing OAuth2 credentials and allows authentication via browse.

## Usage:

```
sbt assembly
java -jar avro-tools/target/scala-2.10/avro-tools-1.8.1.jar tojson <GCS_PATH>
java -jar parquet-tools/target/scala-2.10/parquet-tools-1.8.1.jar cat <GCS_PATH>
```

## How it works:

To make avro-tools and parquet-tools work with GCS we need:
- GCS connector and its dependencies
- GCS connector configuration

GCS connector is not very smart. It does not pick up your gcloud configuration,
and instead expects settings in `core-site.xml`, including:

- register `fs.gs.impl`
- disable service account
- use OAuth2 by using Cloud SDK client (this is NOT user specific)
- give dummy project id (connector will scream otherwise)

```
<configuration>
  <property>
    <name>fs.gs.impl</name>
    <value>com.google.cloud.hadoop.fs.gcs.GoogleHadoopFileSystem</value>
  </property>
  <property>
    <name>fs.gs.auth.service.account.enable</name>
    <value>false</value>
  </property>
  <property>
    <name>fs.gs.auth.client.id</name>
    <value>32555940559.apps.googleusercontent.com</value>
  </property>
  <property>
    <name>fs.gs.auth.client.secret</name>
    <value>ZmssLNjJy2998hD4CTg2ejr2</value>
  </property>
  <property>
    <name>fs.gs.project.id</name>
    <value>foo</value>
  </property>
</configuration>
```
