package randoop.instrument;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.instrument.Instrumentation;
import java.util.Arrays;
import plume.Option;
import plume.Options;

/**
 * The MapCalls javaagent applies the {@link CallReplacementTransformer} to map calls in loaded
 * classes to alternate implementations.
 *
 * <p>Each replacement indicates how the method in a call is to be replaced:
 *
 * <ul>
 *   <li> replacing the method by a specific alternate method;
 *   <li> replacing the method by a method of the same name in a different class; or
 *   <li> replacing the method by a method of the same name, in a class of the same name, but
 *       occurring in a different package.
 * </ul>
 *
 * <p>Replacements are loaded from text files where each line is a pair of method, class, or package
 * names. Both method and class names must be fully qualified.
 *
 * <p>The agent loads default replacements from an internal resource file {@code
 * "replacements.txt"}. User replacements are then loaded using the {@link #map_calls} command-line
 * argument. Any default may be overriden by a user replacement.
 */
public class MapCallsAgent {

  @SuppressWarnings("WeakerAccess")
  @Option("print debug information")
  public static boolean debug = false;

  @SuppressWarnings("WeakerAccess")
  @Option("print progress information")
  public static boolean verbose = false;

  @SuppressWarnings("WeakerAccess")
  @Option("file containing methods calls to map to substitute methods")
  public static File map_calls = null;

  /**
   * Entry point of the java agent. Sets up the transformer {@link CallReplacementTransformer} so
   * that when classes are loaded they are first transformed.
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

    if (agentArgs != null) { // if there are any arguments, parse them
      Options options = new Options(MapCallsAgent.class);
      String[] target_args = options.parse_or_usage(agentArgs);
      if (target_args.length > 0) {
        System.err.printf("Unexpected agent arguments %s%n", Arrays.toString(target_args));
        System.exit(1);
      }
    }

    CallReplacementTransformer transformer = new CallReplacementTransformer();

    InputStream in = transformer.getClass().getResourceAsStream("/replacements.txt");
    if (in == null) {
      System.err.println("Unable to open default replacements file. Please report.");
      System.exit(1);
    }

    // Read the default map file
    try {
      transformer.readMapFile(new InputStreamReader(in), "default-replacements");
    } catch (Throwable e) {
      System.err.printf("Error reading default replacement file:%n  %s%n", e.getMessage());
      System.err.println("  Please report.");
      System.exit(1);
    }

    // Read the user replacement file
    if (map_calls != null) {
      try {
        transformer.readMapFile(map_calls);
      } catch (Throwable e) {
        System.err.printf("Error reading replacement file %s:%n  %s%n", map_calls, e.getMessage());
        System.exit(1);
      }
    }

    transformer.addMapFileShutdownHook();

    inst.addTransformer(transformer);
  }
}
