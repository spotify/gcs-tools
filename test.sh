#!/bin/bash

set -euo pipefail

if [ $# -ne 1 ]; then
    echo "Usage: test.sh gs://temp/location"
    exit 1
fi

GCS=$(mktemp --dry-run --tmpdir=$1 gcs-tools-XXXXXXXXXX)
echo "[INFO] GCS temporary location: $GCS"
OUT=$(mktemp --dry-run --tmpdir= gcs-tools-out-XXXXXXXXXX)
echo "[INFO] Local temporary file: $OUT"

./make-binary.sh

gsutil cp test-files/* $GCS

die() {
    echo "[FAIL] $*"
    echo "============================================================"
    cat $OUT
    echo "============================================================"
    cleanup
    exit 1
}

cleanup() {
    echo "[INFO] Cleaning up $OUT"
    rm $OUT
    echo "[INFO] Cleaning up $GCS"
    gsutil rm -r "$GCS/*"
}

test_cmd() {
    match=$1
    shift
    cmd=$*
    echo "[TEST] $cmd"
    $cmd > $OUT 2>&1
    grep -q "$match" $OUT || die "$cmd"
    echo "[PASS] $cmd"
}

echo "============================================================"

test_cmd 'AvroTools' ./bin/avro-tools getschema $GCS/test.avro
test_cmd '"name":"user100"' ./bin/avro-tools tojson $GCS/test.avro

test_cmd 'AvroTools' ./bin/parquet-cli schema $GCS/test.parquet
test_cmd 'AvroTools' ./bin/parquet-cli meta $GCS/test.parquet
test_cmd '"name": "user100"' ./bin/parquet-cli cat $GCS/test.parquet

test_cmd 'ProtoTools' ./bin/proto-tools getschema $GCS/test.protobuf.avro
test_cmd '"name":"user100"' ./bin/proto-tools tojson $GCS/test.protobuf.avro

test_cmd 'case class AvroTools' ./bin/magnolify-tools avro $GCS/test.avro
test_cmd 'case class AvroTools' ./bin/magnolify-tools parquet $GCS/test.parquet

echo "============================================================"

cleanup
