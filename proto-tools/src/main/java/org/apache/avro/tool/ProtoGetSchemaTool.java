package org.apache.avro.tool;

import org.apache.avro.file.DataFileReader;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.mapred.FsInput;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;

public class ProtoGetSchemaTool implements Tool {

  @Override
  public String getName() {
    return "getschema";
  }

  @Override
  public String getShortDescription() {
    return "Prints out schema of an Protobuf in Avro data file.";
  }

  @Override
  public int run(InputStream in, PrintStream out, PrintStream err,
                 List<String> args) throws Exception {
    if (args.size() != 1) {
      err.println("Expected 1 argument: input_file");
      return 1;
    }
    DataFileReader<Void> reader =
        new DataFileReader<>(new FsInput(new Path(args.get(0)), new Configuration()),
            new GenericDatumReader<Void>());
    out.println(reader.getMetaString("protobuf.generic.schema"));
    return 0;
  }
}
