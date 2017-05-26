package randoop.instrument;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.instrument.Instrumentation;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
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

  @SuppressWarnings("WeakerAccess")
  @Option("file containing list of packages from which classes should not be loaded")
  public static File dont_transform = null;

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

    // Load named default package exclusions from the resource file in the jar
    InputStream inputStream;
    Set<String> excludedPackages = new LinkedHashSet<>();
    inputStream = excludedPackages.getClass().getResourceAsStream("/default-load-exclusions.txt");
    if (inputStream == null) {
      System.err.println("unable to open default package exclusion file. Please report.");
      System.exit(1);
    }
    try {
      loadExclusions(new InputStreamReader(inputStream), excludedPackages);
    } catch (IOException e) {
      System.err.format(
          "Unable to read default package exclusion file: %s%n Please report.", e.getMessage());
      System.exit(1);
    }

    // If user provided package exclusion file, load user package exclusions
    if (dont_transform != null) {
      try {
        loadExclusions(new FileReader(dont_transform), excludedPackages);
      } catch (IOException e) {
        System.err.format(
            "Error reading package exclusion file %s:%n %s%n", dont_transform, e.getMessage());
        System.exit(1);
      }
    }

    CallReplacementTransformer transformer = new CallReplacementTransformer(excludedPackages);

    // Read the default replacement file
    inputStream = transformer.getClass().getResourceAsStream("/replacements.txt");
    if (inputStream == null) {
      System.err.println("Unable to open default replacements file. Please report.");
      System.exit(1);
    }
    try {
      transformer.readMapFile(new InputStreamReader(inputStream), "default-replacements");
    } catch (Throwable e) {
      System.err.printf("Error reading default replacement file:%n  %s%n", e.getMessage());
      System.err.println("  Please report.");
      System.exit(1);
    }

    // If the user provided a replacement file, load user replacements
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

  private static void loadExclusions(Reader exclusionReader, Set<String> excludedPackages)
      throws IOException {
    try (BufferedReader reader = new BufferedReader(exclusionReader)) {
      String line = reader.readLine();
      while (line != null) {
        line = line.replaceFirst("//.*$", "").trim();
        if (line.length() > 0) {
          excludedPackages.add(line);
        }
        line = reader.readLine();
      }
    }
  }
}
