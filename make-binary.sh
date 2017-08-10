#!/bin/bash

#sbt clean assembly

rm -rf bin
mkdir bin
cp avro-tools/target/scala-*/avro-tools-*.jar parquet-tools/target/scala-*/parquet-tools-*.jar proto-tools/target/scala-*/proto-tools-*.jar bin
cp scripts/gcs-tools bin

cd bin
ln -s gcs-tools avro-tools
ln -s gcs-tools parquet-tools
ln -s gcs-tools proto-tools
