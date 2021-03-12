package magnolify.tools;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;

public interface Tool {
  int run(InputStream in, PrintStream out, PrintStream err, List<String> args) throws Exception;
  String getName();

  String getShortDescription();
}