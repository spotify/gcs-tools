#!/bin/bash

sbt clean assembly

rm -rf bin
mkdir bin
cp avro-tools/target/scala-*/avro-tools-*.jar parquet-cli/target/scala-*/parquet-cli-*.jar proto-tools/target/scala-*/proto-tools-*.jar magnolify-tools/target/scala-*/magnolify-tools-*.jar bin
cp scripts/gcs-tools bin

cd bin
ln -s gcs-tools avro-tools
ln -s gcs-tools parquet-cli
ln -s gcs-tools proto-tools
ln -s gcs-tools magnolify-tools
