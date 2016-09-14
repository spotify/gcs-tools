# avro-z

## Raison d'être

### NOTE: this is PoC.

Least amount of change to make avro-tools GCS compatible, and usable from
regular workstation (non-{edgenode,gcp-node}).

The whole trick is about using OAuth2, and allow use to authenticate via browser.

## How to use:

### Assembly:

```
sbt assembly
java -jar target/scala-2.11/avro-z-assembly-0.1.0-SNAPSHOT.jar tojson gs://scio-example/anonym/cleaned/endsong/2016-03-01/00/part-r-00001.avro
```

### Without assembly

#### Use sbt

```
sbt "run tojson gs://scio-example/anonym/cleaned/endsong/2016-03-01/00/part-r-00001.avro"
```

#### Pure java invocation.

This is on my local (check your paths) node:
```
➜  avroz git:(master) ✗ cd src/main/resources
➜  resources git:(master) ✗ ls
core-site.xml
➜  resources git:(master) ✗ pwd
/Users/rav/projects/avroz/src/main/resources
➜  resources git:(master) ✗ java -cp .:/Users/rav/.ivy2/cache/com.google.cloud.bigdataoss/gcs-connector/jars/gcs-connector-1.5.2-hadoop2.jar:/Users/rav/.ivy2/cache/com.google.cloud.bigdataoss/util-hadoop/jars/util-hadoop-1.5.2-hadoop2.jar:/Users/rav/.ivy2/cache/com.google.api-client/google-api-client-java6/jars/google-api-client-java6-1.20.0.jar:/Users/rav/.ivy2/cache/com.google.api-client/google-api-client/jars/google-api-client-1.20.0.jar:/Users/rav/.ivy2/cache/com.google.oauth-client/google-oauth-client/jars/google-oauth-client-1.20.0.jar:/Users/rav/.ivy2/cache/com.google.http-client/google-http-client/jars/google-http-client-1.20.0.jar:/Users/rav/.ivy2/cache/com.google.code.findbugs/jsr305/jars/jsr305-2.0.3.jar:/Users/rav/.ivy2/cache/org.apache.httpcomponents/httpclient/jars/httpclient-4.0.1.jar:/Users/rav/.ivy2/cache/org.apache.httpcomponents/httpcore/jars/httpcore-4.0.1.jar:/Users/rav/.ivy2/cache/commons-logging/commons-logging/jars/commons-logging-1.1.1.jar:/Users/rav/.ivy2/cache/commons-codec/commons-codec/jars/commons-codec-1.3.jar:/Users/rav/.ivy2/cache/com.google.http-client/google-http-client-jackson2/jars/google-http-client-jackson2-1.20.0.jar:/Users/rav/.ivy2/cache/com.fasterxml.jackson.core/jackson-core/jars/jackson-core-2.1.3.jar:/Users/rav/.ivy2/cache/com.google.oauth-client/google-oauth-client-java6/jars/google-oauth-client-java6-1.20.0.jar:/Users/rav/.ivy2/cache/com.google.api-client/google-api-client-jackson2/jars/google-api-client-jackson2-1.20.0.jar:/Users/rav/.ivy2/cache/com.google.apis/google-api-services-storage/jars/google-api-services-storage-v1-rev35-1.20.0.jar:/Users/rav/.ivy2/cache/com.google.guava/guava/bundles/guava-18.0.jar:/Users/rav/.ivy2/cache/com.google.cloud.bigdataoss/util/jars/util-1.5.2.jar:/Users/rav/.ivy2/cache/com.google.cloud.bigdataoss/gcsio/jars/gcsio-1.5.2.jar:/usr/local/Cellar/avro-tools/1.8.1/libexec/avro-tools-1.8.1.jar org.apache.avro.tool.Main tojson gs://scio-example/anonym/cleaned/endsong/2016-03-01/00/part-r-00001.avro
```

