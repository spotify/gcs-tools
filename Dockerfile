FROM mozilla/sbt AS builder

WORKDIR /src

COPY . .

RUN sbt clean assembly

RUN mkdir /out \
  && cp avro-tools/target/scala-*/avro-tools-*.jar /out/avro-tools.jar \
  && cp parquet-tools/target/scala-*/parquet-tools-*.jar /out/parquet-tools.jar \
  && cp proto-tools/target/scala-*/proto-tools-*.jar /out/proto-tools.jar

FROM openjdk:8-jre-slim

COPY --from=builder /out/avro-tools.jar /bin/avro-tools.jar
COPY --from=builder /out/parquet-tools.jar /bin/parquet-tools.jar
COPY --from=builder /out/proto-tools.jar /bin/proto-tools.jar

RUN echo "#!/bin/bash" > /usr/local/bin/avro \
  && echo "java -jar /bin/avro-tools.jar \$@" >> /usr/local/bin/avro \
  && chmod +x /usr/local/bin/avro \
  && echo "#!/bin/bash" > /usr/local/bin/parquet \
  && echo "java -jar /bin/parquet-tools.jar \$@" >> /usr/local/bin/parquet \
  && chmod +x /usr/local/bin/parquet \
  && echo "#!/bin/bash" > /usr/local/bin/proto \
  && echo "java -jar /bin/proto-tools.jar \$@" >> /usr/local/bin/proto \
  && chmod +x /usr/local/bin/proto