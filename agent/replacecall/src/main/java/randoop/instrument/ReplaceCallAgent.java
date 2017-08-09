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
 * The replacecall javaagent applies the {@link CallReplacementTransformer} to replace calls in
 * loaded classes by calls to alternate implementations.
 *
 * <p>The transformer applies method call replacements as specified in either the default or a
 * user-provided replacement file. (See the <a
 * href="https://randoop.github.io/randoop/manual/index.html#replacecall">replacecall user
 * documentation</a> the file format.) Default replacements are given in an internal resource file
 * {@code "default-replacements.txt"}. User replacements are then loaded using the {@code
 * --map-calls} command-line argument. A user replacement may override a default replacement.
 *
 * <p>The classes of packages listed in the resource file {@code "default-load-exclusions.txt"} are
 * excluded from transformation.
 */
public class ReplaceCallAgent {

  /** Run the replacecall agent in debug mode. */
  @SuppressWarnings("WeakerAccess")
  @Option("print debug information")
  public static boolean debug = false;

  @SuppressWarnings("WeakerAccess")
  @Option("directory name where debug logs are written")
  public static String debug_directory = null;

  static Path debugPath;

  @SuppressWarnings("WeakerAccess")
  @Option("print progress information")
  public static boolean verbose = false;

  /** The file from which to read the user replacements for replacing calls. */
  @SuppressWarnings("WeakerAccess")
  @Option("file listing methods whose calls to replace by substitute methods")
  public static File map_calls = null;

  /** Exclude transformation of classes in the the listed packages. */
  @SuppressWarnings("WeakerAccess")
  @Option("file listing packages whose classes should not be transformed")
  public static File dont_transform = null;

  /**
   * Entry point of the replacecall Java agent. Initializes the {@link CallReplacementTransformer}
   * so that when classes are loaded they are transformed to replace calls to methods as specified
   * in the replacements file(s).
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

    if (agentArgs != null) { // If there are any arguments, parse them
      Options options = new Options(ReplaceCallAgent.class);
      String[] target_args = options.parse_or_usage(agentArgs);
      if (target_args.length > 0) {
        System.err.printf("Unexpected agent arguments %s%n", Arrays.toString(target_args));
        System.exit(1); // Exit on bad user input.
      }
    }

    debugPath = Paths.get("").toAbsolutePath().normalize();
    if (debug && debug_directory != null && !debug_directory.isEmpty()) {
      debugPath = debugPath.resolve(debug_directory);
      if (!Files.exists(debugPath)) {
        Files.createDirectory(debugPath);
      }
    }

    // Load package prefixes from the resource file in the jar for default package exclusions
    Set<String> excludedPackagePrefixes = new LinkedHashSet<>();

    String exclusionFileName = "/default-load-exclusions.txt";
    InputStream inputStream = ReplaceCallAgent.class.getResourceAsStream(exclusionFileName);
    if (inputStream == null) {
      System.err.println("Unable to find default package exclusion file. Please report.");
      System.exit(1); // Exit on configuration error. (See note at end of method.)
    }
    try {
      loadExclusions(
          new InputStreamReader(inputStream), exclusionFileName, excludedPackagePrefixes);
    } catch (IOException e) {
      System.err.format(
          "Unable to read default package exclusion file: %s%nPlease report.", e.getMessage());
      System.exit(1); // Exit on configuration error. (See note at end of method.)
    }

    // If user-provided package exclusion file, load user package exclusions
    if (dont_transform != null) {
      try {
        loadExclusions(
            new FileReader(dont_transform), dont_transform.getName(), excludedPackagePrefixes);
      } catch (IOException e) {
        System.err.format(
            "Error reading package exclusion file %s:%n %s%n", dont_transform, e.getMessage());
        System.exit(1); // Exit on user input error.
      }

      // Within Randoop, the policy for handling configuration errors is that a BugInRandoopException
      // should be thrown. However, the agent is separate from Randoop, and an uncaught exception
      // within premain results in the JVM halting. So, this method is explicit in the call to
      // System.exit() when a configuration error results in a failure.

    }

    /*
     * The agent is called when classes are loaded. If Randoop is using threads, this can result in
     * multiple threads accessing the map to apply replacements. Since
     */
    ConcurrentHashMap<MethodSignature, MethodSignature> replacementMap = new ConcurrentHashMap<>();

    // Read the default replacement file
    String replacementPath = "/default-replacements.txt";
    inputStream = ReplaceCallAgent.class.getResourceAsStream(replacementPath);
    if (inputStream == null) {
      System.err.println("Unable to open default replacements file. Please report.");
      System.exit(1);
    }
    try {
      replacementMap =
          ReplacementFileReader.readReplacements(
              new InputStreamReader(inputStream), replacementPath);
    } catch (ReplacementFileException e) {
      System.err.printf("Error reading default replacement file:%n  %s%n", e);
      System.err.println("Check that replacecall.jar is on the classpath or bootclasspath.");
      System.exit(1);
    }

    // If the user has provided a replacement file, load user replacements and put them into the
    // map, possibly overriding default replacements that already appear in the map.
    if (map_calls != null) {
      try {
        replacementMap.putAll(ReplacementFileReader.readReplacements(map_calls));
      } catch (Throwable e) {
        System.err.printf("Error reading replacement file %s:%n  %s%n", map_calls, e.getMessage());
        System.exit(1);
      }
    }

    // Communicate the list of replaced methods to Randoop to omit direct calls
    List<String> signatureList = new ArrayList<>();
    for (MethodSignature def : replacementMap.keySet()) {
      signatureList.add(def.toString());
    }
    MethodReplacements.addReplacedMethods(signatureList);

    CallReplacementTransformer transformer =
        new CallReplacementTransformer(replacementMap, excludedPackagePrefixes);
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
   * @param excludedPackagePrefixes the set of excluded package prefixes, modified by adding new
   *     prefixes. Must not be null.
   * @throws IOException if there is an error reading the file
   */
  private static void loadExclusions(
      Reader exclusionReader, String filename, Set<String> excludedPackagePrefixes)
      throws IOException {
    try (EntryReader reader = new EntryReader(exclusionReader, filename, "//.*$", null)) {
      for (String line : reader) {
        String trimmed = line.trim();
        if (!trimmed.isEmpty()) {
          if (trimmed.charAt(trimmed.length() - 1) != '.') {
            trimmed = trimmed + ".";
          }
          excludedPackagePrefixes.add(trimmed);
        }
      }
    }
  }
}
