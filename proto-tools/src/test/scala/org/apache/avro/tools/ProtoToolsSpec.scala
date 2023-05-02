package org.apache.avro.tools

import me.lyh.protobuf
import org.apache.avro.Schema
import org.apache.avro.file.DataFileWriter
import org.apache.avro.generic.{GenericData, GenericDatumWriter, GenericRecord}
import org.apache.avro.tool.{ProtoGetSchemaTool, ProtoToJsonTool}
import org.apache.commons.io.input.NullInputStream
import org.scalatest.{BeforeAndAfterAll, Outcome}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, File, PrintStream}
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import scala.jdk.CollectionConverters._
import scala.util.chaining._

object ProtoToolsSpec {
  val ProtobufAvroSchema: Schema = new Schema.Parser().parse("""{
      |  "name": "AvroBytesRecord",
      |  "type": "record",
      |  "fields": [
      |    {"name": "bytes", "type": "bytes"}
      |  ]
      |}
      |""".stripMargin)
}

class ProtoToolsSpec extends AnyFlatSpec with Matchers with BeforeAndAfterAll {

  import ProtoToolsSpec._

  val data = List(
    Test.Person.newBuilder().setName("Bob").setAge(25).build(),
    Test.Person.newBuilder().setName("Alice").setAge(27).build()
  )

  private def createTestData(): File = {
    val file = Files.createTempFile("proto-tools", ".proto.avro").toFile
    val datumWriter = new GenericDatumWriter[GenericRecord](ProtobufAvroSchema)
    val dataFileWriter = new DataFileWriter(datumWriter)
    dataFileWriter.setMeta(
      "protobuf.generic.schema",
      protobuf.generic.Schema.of[Test.Person].toJson
    )
    dataFileWriter.create(ProtobufAvroSchema, file)
    val avroRecord = new GenericData.Record(ProtobufAvroSchema)
    data.foreach { p =>
      avroRecord.put("bytes", ByteBuffer.wrap(p.toByteArray))
      dataFileWriter.append(avroRecord)
    }
    dataFileWriter.close()
    file
  }

  private var protoFile: File = _

  def withOutputs(testCode: (ByteArrayOutputStream, ByteArrayOutputStream) => Any): Unit = {
    val out = new ByteArrayOutputStream()
    val err = new ByteArrayOutputStream()
    try {
      testCode(out, err)
    } finally {
      out.close()
      err.close()
    }
  }

  override def beforeAll(): Unit =
    protoFile = createTestData()

  "ProtoTools" should "print proto schema" in withOutputs { (out, err) =>
    val schemaTool = new ProtoGetSchemaTool()
    val args = List(protoFile.getPath)
    val result = schemaTool.run(
      new NullInputStream(),
      new PrintStream(out, true),
      new PrintStream(err, true),
      args.asJava
    )
    withClue(err.toString(StandardCharsets.UTF_8))(result shouldBe 0)
    val json = out.toString(StandardCharsets.UTF_8)
    json shouldBe """{"name":"proto3.Person","messages":[""" +
      """{"name":"proto3.Person","fields":[""" +
      """{"id":1,"name":"name","label":"OPTIONAL","type":"STRING","packed":false,"default":null,"schema":null,"options":null},""" +
      """{"id":2,"name":"age","label":"OPTIONAL","type":"INT64","packed":false,"default":null,"schema":null,"options":null}""" +
      """],"options":null}],"enums":[]}""" +
      "\n"
  }

  it should "print data as json" in withOutputs { (out, err) =>
    val jsonTool = new ProtoToJsonTool()
    val args = List(protoFile.getPath)

    val result = jsonTool.run(
      new NullInputStream(),
      new PrintStream(out, true),
      new PrintStream(err, true),
      args.asJava
    )
    withClue(err.toString(StandardCharsets.UTF_8))(result shouldBe 0)
    val json = out.toString(StandardCharsets.UTF_8)
    json shouldBe """{"name":"Bob","age":25}
                    |{"name":"Alice","age":27}
                    |
                    |""".stripMargin
  }

}
