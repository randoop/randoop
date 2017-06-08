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
 * <p>The transformer applies method call replacements as specified in either the default or a user
 * provided replacement file. (See {@link CallReplacementTransformer#readMapFile(Reader)} for
 * details on the file format.) Default replacements are given in an internal resource file {@code
 * "default-replacements.txt"}. User replacements are then loaded using the {@link #map_calls
 * --map-calls} command-line argument. A user replacement may override a default replacement.
 *
 * <p>The classes of certain packages are excluded from transformation. These exclusions include
 * boot loaded classes (see {@link CallReplacementTransformer#isBootClass(ClassLoader, String)})
 * that are not in AWT or Swing (see {@link CallReplacementTransformer#isAWTSwingClass(String)}),
 * and classes in packages listed in the resource file {@code "default-load-exclusions.txt"}.
 */
public class MapCallsAgent {

  /** Run the mapcall agent in debug mode. */
  @SuppressWarnings("WeakerAccess")
  @Option("print debug information")
  public static boolean debug = false;

  @SuppressWarnings("WeakerAccess")
  @Option("print progress information")
  public static boolean verbose = false;

  /** The file from which to read the user replacements for mapping calls. */
  @SuppressWarnings("WeakerAccess")
  @Option("file containing methods whose calls to replace by substitute methods")
  public static File map_calls = null;

  /** Exclude transformation of classes in the the listed packages. */
  @SuppressWarnings("WeakerAccess")
  @Option("file containing list of packages whose classes should not be transformed")
  public static File dont_transform = null;

  /**
   * Entry point of the mapcall java agent. Initializes the {@link CallReplacementTransformer} so
   * that when classes are loaded they are transformed to replace calls to methods as specified in
   * the replacements file(s).
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
    Set<String> excludedPackages = new LinkedHashSet<>();
    InputStream inputStream =
        excludedPackages.getClass().getResourceAsStream("/default-load-exclusions.txt");
    if (inputStream == null) {
      System.err.println("unable to open default package exclusion file. Please report.");
      System.exit(1);
    }
    try {
      loadExclusions(new InputStreamReader(inputStream), excludedPackages);
    } catch (IOException e) {
      System.err.format(
          "Unable to read default package exclusion file: %s%nPlease report.", e.getMessage());
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
    inputStream = transformer.getClass().getResourceAsStream("/default-replacements.txt");
    if (inputStream == null) {
      System.err.println("Unable to open default replacements file. Please report.");
      System.exit(1);
    }
    try {
      transformer.readMapFile(new InputStreamReader(inputStream));
    } catch (Throwable e) {
      System.err.printf("Error reading default replacement file:%n  %s%n", e);
      System.err.println("Please report.");
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

  /**
   * Load package names from the given file and add them to the set of excluded package names.
   *
   * @param exclusionReader the reader for the text file containing the list of excluded packages,
   *     must not be null
   * @param excludedPackages the set of excluded package names, must not be null
   * @throws IOException if there is an error reading the file
   */
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
