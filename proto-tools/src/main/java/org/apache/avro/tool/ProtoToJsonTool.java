package org.apache.avro.tool;

import static com.google.common.base.Preconditions.checkNotNull;
import com.fasterxml.jackson.databind.ObjectMapper;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.apache.avro.file.DataFileStream;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.util.List;

public class ProtoToJsonTool implements Tool {

  @Override
  public String getName() {
    return "tojson";
  }

  @Override
  public String getShortDescription() {
    return "Dumps a Protobuf in Avro data file as JSON, record per line or pretty.";
  }

  @Override
  public int run(InputStream in, PrintStream out, PrintStream err,
                 List<String> args) throws Exception {
    OptionParser optionParser = new OptionParser();
    OptionSpec<Void> prettyOption = optionParser
        .accepts("pretty", "Turns on pretty printing.");

    OptionSet optionSet = optionParser.parse(args.toArray(new String[0]));
    Boolean pretty = optionSet.has(prettyOption);
    List<String> nargs = (List<String>)optionSet.nonOptionArguments();

    if (nargs.size() != 1) {
      printHelp(err);
      err.println();
      optionParser.printHelpOn(err);
      return 1;
    }

    BufferedInputStream inStream = Util.fileOrStdin(nargs.get(0), in);

    GenericDatumReader<Object> reader = new GenericDatumReader<>();
    DataFileStream<Object> streamReader = new DataFileStream<>(inStream, reader);
    ObjectMapper mapper = new ObjectMapper();
    try {
      String schema = streamReader.getMetaString("protobuf.generic.schema");
      checkNotNull(schema, "Missing metadata key protobuf.generic.schema");
      ProtobufReader protoReader = new ProtobufReader(schema);
      for (Object datum : streamReader) {
        ByteBuffer byteBuffer = (ByteBuffer) ((GenericRecord) datum).get("bytes");
        String json = protoReader.toJson(byteBuffer);
        if (pretty) {
          String prettyJson = mapper
              .writerWithDefaultPrettyPrinter()
              .writeValueAsString(mapper.readValue(json, Object.class));
          out.println(prettyJson);
        } else {
          out.println(json);
        }
      }
      out.println();
      out.flush();
    } finally {
      streamReader.close();
    }
    return 0;
  }

  private void printHelp(PrintStream ps) {
    ps.println("tojson --pretty input-file");
    ps.println();
    ps.println(getShortDescription());
    ps.println("A dash ('-') can be given as an input file to use stdin");
  }
}
