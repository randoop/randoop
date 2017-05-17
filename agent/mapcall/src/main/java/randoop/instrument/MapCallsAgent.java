package randoop.instrument;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.instrument.Instrumentation;
import java.util.Arrays;
import plume.Option;
import plume.Options;

public class Premain {

  @Option("print debug information")
  public static boolean debug = false;

  @Option("print progress information")
  public static boolean verbose = false;

  @Option("file containing methods calls to map to substitute methods")
  public static File map_calls = null;

  /**
   * Entry point of the java agent. Sets up the transformer {@link Instrument} so that when classes
   * are loaded they are first transformed.
   *
   * @param agentArgs the arguments to the agent
   * @param inst the {@code Instrumentation} object
   * @throws IOException if there is an error reading the map file
   */
  public static void premain(String agentArgs, Instrumentation inst) throws IOException {

    if (verbose) {
      System.out.format(
          "In premain, agentargs ='%s', " + "Instrumentation = '%s'%n", agentArgs, inst);
    }

    // Parse our arguments
    Options options = new Options(Premain.class);
    String[] target_args = options.parse_or_usage(agentArgs);
    if (target_args.length > 0) {
      System.err.printf("Unexpected agent arguments %s%n", Arrays.toString(target_args));
      System.exit(1);
    }

    Instrument transformer = new Instrument();

    InputStream in = transformer.getClass().getResourceAsStream("replacements.txt");

    // Read the default map file
    transformer.readMapFile(new InputStreamReader(in), "default-replacements");

    // Read the user replacement file
    if (map_calls != null) {
      transformer.readMapFile(map_calls);
      transformer.addMapFileShutdownHook();
    }

    // Instrument transformer = new Instrument();
    inst.addTransformer(transformer);
  }
}
