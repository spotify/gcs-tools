package magnolify.tools;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

public class Main {
  final Map<String, Tool> tools;

  int maxLen = 0;

  Main() {
    tools = new TreeMap<String, Tool>();
    for (Tool tool : new Tool[] {
        new MagnolifyAvroTool(),
        new MagnolifyParquetTool()
    }) {
      Tool prev = tools.put(tool.getName(), tool);
      if (prev != null) {
        throw new AssertionError(
            "Two tools with identical names: " + tool + ", " + prev);
      }
      maxLen = Math.max(tool.getName().length(), maxLen);
    }
  }

  public static void main(String[] args) throws Exception {
    int rc = new Main().run(args);
    System.exit(rc);
  }

  private int run(String[] args) throws Exception {
    if (args.length != 0) {
      Tool tool = tools.get(args[0]);
      if (tool != null) {
        return tool.run(
            System.in, System.out, System.err, Arrays.asList(args).subList(1, args.length));
      }
    }

    System.err.println("Magnolify Tools");
    System.err.println("----------------");

    System.err.println("Available tools:");
    for (Tool k : tools.values()) {
      System.err.printf("%" + maxLen + "s  %s\n", k.getName(), k.getShortDescription());
    }

    return 1;
  }
}
