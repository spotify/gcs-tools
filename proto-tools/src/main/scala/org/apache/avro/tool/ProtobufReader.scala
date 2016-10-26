package org.apache.avro.tool

import java.nio.ByteBuffer

import me.lyh.protobuf.generic._

class ProtobufReader(val schema: String) {
  private val reader = GenericReader.of(Schema.fromJson(schema))
  def toJson(byteBuffer: ByteBuffer): String = reader.read(byteBuffer).toJson
}
