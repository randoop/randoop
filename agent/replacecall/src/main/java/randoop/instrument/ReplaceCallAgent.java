package randoop.instrument;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.instrument.Instrumentation;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.plumelib.options.Option;
import org.plumelib.options.Options;
import org.plumelib.util.EntryReader;
import org.plumelib.util.UtilPlume;
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
 * --replacement-file} command-line argument. A user replacement may override a default replacement.
 *
 * <p>The classes of packages listed in the resource file {@code "default-load-exclusions.txt"} are
 * excluded from transformation.
 */
public class ReplaceCallAgent {

  /** The name of this agent. */
  private static final String AGENT_NAME = "replacecall";

  /** Run the replacecall agent in debug mode. */
  @SuppressWarnings("WeakerAccess")
  @Option("print debug information")
  public static boolean debug = false;

  /**
   * The directory where debug logs are written when {@code debug} is set. If no directory is given,
   * then the current working directory is used.
   */
  @SuppressWarnings("WeakerAccess")
  @Option("directory name where debug logs are written")
  public static String debug_directory;

  /** The path for the debug directory. Used by the logs in {@link CallReplacementTransformer}. */
  static Path debugPath;

  @SuppressWarnings("WeakerAccess")
  @Option("print progress information")
  public static boolean verbose = false;

  /** The file from which to read the user replacements for replacing calls. */
  @SuppressWarnings("WeakerAccess")
  @Option("file listing methods whose calls to replace by substitute methods")
  public static Path replacement_file = null;

  /** Exclude transformation of classes in the the listed packages. */
  @SuppressWarnings("WeakerAccess")
  @Option("file listing packages whose classes should not be transformed")
  public static Path dont_transform = null;

  /**
   * Entry point of the replacecall Java agent. Initializes the {@link CallReplacementTransformer}
   * so that when classes are loaded they are transformed to replace calls to methods as specified
   * in the replacements file(s).
   *
   * @param agentArgs the arguments to the agent
   * @param instrumentation the {@code Instrumentation} object
   * @throws IOException if there is an error reading a file
   */
  public static void premain(String agentArgs, Instrumentation instrumentation) throws IOException {
    try {
      if (agentArgs != null) { // If there are any arguments, parse them
        Options options = new Options(ReplaceCallAgent.class);
        String[] target_args = options.parse(true, Options.tokenize(agentArgs));
        if (target_args.length > 0) {
          System.err.printf("Unexpected agent arguments %s%n", Arrays.toString(target_args));
          System.exit(1); // Exit on bad user input.
        }
      }

      if (verbose) {
        System.out.format(
            "In premain, agentargs ='%s', " + "Instrumentation = '%s'%n",
            agentArgs, instrumentation);
      }

      debugPath = Paths.get("").toAbsolutePath().toAbsolutePath();
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
        throw new BugInAgentException("Unable to find default package exclusion file.");
      }
      try {
        excludedPackagePrefixes.addAll(
            loadExclusions(new InputStreamReader(inputStream), exclusionFileName));
      } catch (IOException e) {
        throw new BugInAgentException(
            "Unable to read default package exclusion file: " + e.getMessage());
      }

      // If user-provided package exclusion file, load user package exclusions
      Path exclusionFilePath = null;
      if (dont_transform != null) {
        try {
          excludedPackagePrefixes.addAll(
              loadExclusions(
                  Files.newBufferedReader(dont_transform, StandardCharsets.UTF_8),
                  dont_transform.toString()));
        } catch (IOException e) {
          System.err.format(
              "Error reading package exclusion file %s:%n %s%n", dont_transform, e.getMessage());
          System.exit(1); // Exit on user input error. (Throwing exception would halt JVM.)
        }
        // Get path for exclusions file to use in argument string given to Randoop
        exclusionFilePath = dont_transform;
      }

      /*
       * The agent is called when classes are loaded. If Randoop is using threads, this can result
       * in multiple threads accessing the map to apply replacements.
       */
      HashMap<MethodSignature, MethodSignature> replacementMap;

      // Read the default replacement file
      String replacementPath = "/default-replacements.txt";
      inputStream = ReplaceCallAgent.class.getResourceAsStream(replacementPath);
      if (inputStream == null) {
        throw new BugInAgentException("Unable to open default replacements file.");
      }
      try {
        replacementMap =
            ReplacementFileReader.readReplacements(
                new InputStreamReader(inputStream), replacementPath);
      } catch (ReplacementFileException e) {
        throw new BugInAgentException("Error reading default replacement file. " + e.getMessage());
      }

      // If the user has provided a replacement file, load user replacements and put them into the
      // map, possibly overriding default replacements that already appear in the map.
      Path replacementFilePath = null;
      if (replacement_file != null) {
        try {
          replacementMap.putAll(ReplacementFileReader.readReplacements(replacement_file));
        } catch (Throwable e) {
          System.err.printf(
              "Error reading replacement file %s:%n  %s%n", replacement_file, e.getMessage());
          System.exit(1);
        }
        // Get path for replacement file to use in argument string given to Randoop.
        replacementFilePath = replacement_file;
      }

      /*
       * The flaky-filter in Randoop needs to run the generated tests using the agent with the same
       * file inputs on the command-line. The paths for the files are made absolute and then the
       * argument string is rebuilt.
       */
      MethodReplacements.setAgentPath(getAgentPath());
      MethodReplacements.setAgentArgs(createAgentArgs(replacementFilePath, exclusionFilePath));

      // Communicate the list of replaced methods to Randoop to omit direct calls
      List<String> signatureList = new ArrayList<>();
      for (MethodSignature def : replacementMap.keySet()) {
        signatureList.add(def.toString());
      }
      MethodReplacements.setReplacedMethods(signatureList);

      // Create the transformer and add to the class loader instrumentation
      CallReplacementTransformer transformer =
          new CallReplacementTransformer(replacementMap, excludedPackagePrefixes);
      transformer.addMapFileShutdownHook();
      instrumentation.addTransformer(transformer);

    } catch (BugInAgentException e) {
      System.err.println("Error in replacecall agent: " + e.getMessage());
      System.err.println(
          "For problems with the default replacements file, make sure that the"
              + " replacecall.jar file is on the bootclasspath.");
      System.err.println("Otherwise, please report at https://github.com/randoop/randoop/issues .");
      System.exit(1);
    } catch (Throwable e) {
      // Make sure that a message is printed for any stray exception
      System.err.println("Unexpected exception thrown by replacecall agent: " + e.getMessage());
      System.err.println("Please report at https://github.com/randoop/randoop/issues .");
      System.exit(1);
    }
  }

  /**
   * Load package names from the given file and add them to the set of excluded package names. Adds
   * a period to the end of any name that does not have one.
   *
   * @param exclusionReader the reader for the text file containing the list of excluded packages,
   *     must not be null
   * @param filename the name of the file read by the reader
   * @return the set of excluded package prefixes from the file
   * @throws IOException if there is an error reading the file
   */
  private static Set<String> loadExclusions(Reader exclusionReader, String filename)
      throws IOException {
    Set<String> excludedPackagePrefixes = new LinkedHashSet<>();
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
    return excludedPackagePrefixes;
  }

  /**
   * Extracts the path for the jar of this agent from the boot classpath.
   *
   * @return the path for the jar file, as a string
   * @throws BugInAgentException if the agent is not found on the boot classpath
   */
  private static String getAgentPath() throws BugInAgentException {
    String bootclasspath = System.getProperty("sun.boot.class.path");
    String[] paths = bootclasspath.split(java.io.File.pathSeparator);
    for (String path : paths) {
      if (path.contains(AGENT_NAME)) {
        return path;
      }
    }
    throw new BugInAgentException("Agent should be included on bootclasspath");
  }

  /**
   * Creates an argument string using absolute paths for the replacement and exclusion file.
   *
   * <p>This is necessary because the flaky filter in Randoop needs to call the agent using the same
   * files used in the original use of the agent.
   *
   * @param replacementFilePath the {@code Path} for the replacement file
   * @param exclusionFilePath the {@code Path} for the replacement file
   * @return the argument string for the current run using absolute paths
   */
  private static String createAgentArgs(Path replacementFilePath, Path exclusionFilePath) {
    List<String> args = new ArrayList<>();
    if (replacementFilePath != null) {
      args.add("--replacement-file=" + replacementFilePath.toAbsolutePath());
    }
    if (exclusionFilePath != null) {
      args.add("--dont-transform=" + exclusionFilePath.toAbsolutePath());
    }
    return UtilPlume.join(args, ",");
  }

  /**
   * Private exception class used to manage agent-specific errors.
   *
   * <p>Analogous to {@code BugInRandoopException}, but that class is not available within the
   * agent.
   */
  private static class BugInAgentException extends Throwable {

    /**
     * Create a {@link BugInAgentException} with the message.
     *
     * @param message the error message
     */
    BugInAgentException(String message) {
      super(message);
    }
  }
}
