GCS Tools
=========

[![Build Status](https://img.shields.io/github/workflow/status/spotify/gcs-tools/CI)](https://github.com/spotify/gcs-tools/actions?query=workflow%3ACI)
[![GitHub license](https://img.shields.io/github/license/spotify/gcs-tools.svg)](./LICENSE)

## Raison d'être:

Light weight wrapper that adds [Google Cloud Storage](https://cloud.google.com/storage/) (GCS) support to common Hadoop tools, including [avro-tools](https://mvnrepository.com/artifact/org.apache.avro/avro-tools), [parquet-cli](https://mvnrepository.com/artifact/org.apache.parquet/parquet-cli), proto-tools for [Scio](https://github.com/spotify/scio)'s Protobuf in Avro file, and magnolify-tools for [Magnolify](https://github.com/spotify/magnolify) code generation, so that they can be used from regular workstations or laptops, outside of a [Google Compute Engine](https://cloud.google.com/compute/) (GCE) instance.

It uses your existing OAuth2 credentials and allows authentication via a browser.

## Usage:

You can install the tools via our [Homebrew tap](https://github.com/spotify/homebrew-public) on Mac.

```
brew tap spotify/public
brew install gcs-avro-tools gcs-parquet-cli gcs-proto-tools gcs-magnolify-tools
avro-tools tojson <GCS_PATH>
parquet-cli cat <GCS_PATH>
proto-tools tojson <GCS_PATH>
magnolify-tools <avro|parquet> <GCS_PATH>
```

Or build them yourself.

```
sbt assembly
java -jar avro-tools/target/scala-2.13/avro-tools-*.jar tojson <GCS_PATH>
java -jar parquet-cli/target/scala-2.13/parquet-cli-*.jar cat <GCS_PATH>
java -jar proto-tools/target/scala-2.13/proto-tools-*.jar cat <GCS_PATH>
java -jar magnolify-tools/target/scala-2.13/magnolify-tools-*.jar <avro|parquet> <GCS_PATH>
```

## How it works:

To make avro-tools and parquet-cli work with GCS we need:
- [GCS connector](https://github.com/GoogleCloudPlatform/bigdata-interop) and its dependencies
- [GCS connector configuration](//github.com/spotify/gcs-tools/blob/master/shared/src/main/resources/core-site.xml)

GCS connector won't pick up your local gcloud configuration, and instead expects settings
in [core-site.xml](https://github.com/spotify/gcs-tools/blob/master/shared/src/main/resources/core-site.xml).
