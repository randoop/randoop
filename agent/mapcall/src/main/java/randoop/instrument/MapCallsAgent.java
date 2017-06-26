package randoop.instrument;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.instrument.Instrumentation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import plume.EntryReader;
import plume.Option;
import plume.Options;
import randoop.MethodReplacements;

/**
 * The MapCalls javaagent applies the {@link CallReplacementTransformer} to map calls in loaded
 * classes to alternate implementations.
 *
 * <p>The transformer applies method call replacements as specified in either the default or a user
 * provided replacement file. (See the <a
 * href="https://randoop.github.io/randoop/manual/index.html#map_calls">mapcall user documentat</a>
 * for details on the file format.) Default replacements are given in an internal resource file
 * {@code "default-replacements.txt"}. User replacements are then loaded using the {@link #map_calls
 * --map-calls} command-line argument. A user replacement may override a default replacement.
 *
 * <p>The classes of packages listed in the resource file {@code "default-load-exclusions.txt"} are
 * excluded from transformation.
 */
public class MapCallsAgent {

  /** Run the mapcall agent in debug mode. */
  @SuppressWarnings("WeakerAccess")
  @Option("print debug information")
  public static boolean debug = false;

  @SuppressWarnings("WeakerAccess")
  @Option("directory name for debug information")
  public static String debug_directory = "";

  static Path debugPath = Paths.get("").toAbsolutePath().normalize();

  @SuppressWarnings("WeakerAccess")
  @Option("print progress information")
  public static boolean verbose = false;

  /** The file from which to read the user replacements for mapping calls. */
  @SuppressWarnings("WeakerAccess")
  @Option("file listing methods whose calls to replace by substitute methods")
  public static File map_calls = null;

  /** Exclude transformation of classes in the the listed packages. */
  @SuppressWarnings("WeakerAccess")
  @Option("file listing packages whose classes should not be transformed")
  public static File dont_transform = null;

  /**
   * Entry point of the mapcall Java agent. Initializes the {@link CallReplacementTransformer} so
   * that when classes are loaded they are transformed to replace calls to methods as specified in
   * the replacements file(s).
   *
   * @param agentArgs the arguments to the agent
   * @param inst the {@code Instrumentation} object
   * @throws IOException if there is an error reading a file
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

    if (debug && !debug_directory.isEmpty()) {
      debugPath = debugPath.resolve(debug_directory);
      if (!Files.exists(debugPath)) {
        Files.createDirectory(debugPath);
      }
    }

    // Load named default package exclusions from the resource file in the jar
    Set<String> excludedPackages = new LinkedHashSet<>();

    String exclusionFileName = "/default-load-exclusions.txt";
    InputStream inputStream = MapCallsAgent.class.getResourceAsStream(exclusionFileName);
    if (inputStream == null) {
      System.err.println("unable to open default package exclusion file. Please report.");
      System.exit(1);
    }
    try {
      loadExclusions(new InputStreamReader(inputStream), exclusionFileName, excludedPackages);
    } catch (IOException e) {
      System.err.format(
          "Unable to read default package exclusion file: %s%nPlease report.", e.getMessage());
      System.exit(1);
    }

    // If user provided package exclusion file, load user package exclusions
    if (dont_transform != null) {
      try {
        loadExclusions(new FileReader(dont_transform), dont_transform.getName(), excludedPackages);
      } catch (IOException e) {
        System.err.format(
            "Error reading package exclusion file %s:%n %s%n", dont_transform, e.getMessage());
        System.exit(1);
      }
    }

    ConcurrentHashMap<MethodDef, MethodDef> replacementMap = new ConcurrentHashMap<>();

    // Read the default replacement file
    inputStream = MapCallsAgent.class.getResourceAsStream("/default-replacements.txt");
    if (inputStream == null) {
      System.err.println("Unable to open default replacements file. Please report.");
      System.exit(1);
    }
    try {
      replacementMap = ReplacementFileReader.readFile(new InputStreamReader(inputStream));
    } catch (Throwable e) {
      System.err.printf("Error reading default replacement file:%n  %s%n", e);
      System.err.println("Check that the mapcall.jar is on the classpath or bootclasspath.");
      System.exit(1);
    }

    // If the user has provided a replacement file, load user replacements and put them into the
    // map for the default replacements. This use of the Map.put method allows a user replacement to
    // override a default replacement.
    if (map_calls != null) {
      try {
        replacementMap.putAll(ReplacementFileReader.readFile(map_calls));
      } catch (Throwable e) {
        System.err.printf("Error reading replacement file %s:%n  %s%n", map_calls, e.getMessage());
        System.exit(1);
      }
    }

    // Communicate the list of replaced methods to Randoop
    List<String> signatureList = new ArrayList<>();
    for (MethodDef def : replacementMap.keySet()) {
      signatureList.add(def.toString());
    }
    MethodReplacements.addReplacedMethods(signatureList);

    CallReplacementTransformer transformer =
        new CallReplacementTransformer(excludedPackages, replacementMap);
    transformer.addMapFileShutdownHook();

    inst.addTransformer(transformer);
  }

  /**
   * Load package names from the given file and add them to the set of excluded package names. Adds
   * a period to the end of any name that does not have one.
   *
   * @param exclusionReader the reader for the text file containing the list of excluded packages,
   *     must not be null
   * @param filename the name of the file read by the reader
   * @param excludedPackages the set of excluded package names, must not be null
   * @throws IOException if there is an error reading the file
   */
  private static void loadExclusions(
      Reader exclusionReader, String filename, Set<String> excludedPackages) throws IOException {
    try (EntryReader reader = new EntryReader(exclusionReader, filename, "//.*$", null)) {
      for (String line : reader) {
        String trimmed = line.trim();
        if (!trimmed.isEmpty()) {
          if (trimmed.charAt(trimmed.length() - 1) != '.') {
            trimmed = trimmed + ".";
          }
          excludedPackages.add(trimmed);
        }
      }
    }
  }
}
