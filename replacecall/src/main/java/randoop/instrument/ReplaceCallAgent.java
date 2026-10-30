package randoop.instrument;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.instrument.Instrumentation;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import org.checkerframework.checker.mustcall.qual.Owning;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.plumelib.options.Option;
import org.plumelib.options.Options;
import org.plumelib.util.CollectionsPlume;
import randoop.MethodReplacements;

/**
 * The replacecall javaagent applies the {@link CallReplacementTransformer} to replace calls in
 * loaded classes by calls to alternate implementations.
 *
 * <p>The transformer applies method call replacements as specified in either the default or a
 * user-provided replacement file. (See the <a
 * href="https://randoop.github.io/randoop/manual/index.html#replacecall">replacecall user
 * documentation</a> for the file format.) Default replacements are given in an internal resource
 * file {@code "default-replacements.txt"}. User replacements are then loaded using the {@code
 * --replacement-file} command-line argument. A user replacement may override a default replacement.
 *
 * <p>The classes of packages listed in the resource file {@code "default-load-exclusions.txt"} are
 * excluded from transformation.
 */
public final class ReplaceCallAgent {

  /** Do not instantiate. */
  private ReplaceCallAgent() {
    throw new UnsupportedOperationException("Do not instantiate");
  }

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
  public static @MonotonicNonNull String debug_directory;

  /** The path for the debug directory. Used by the logs in {@link CallReplacementTransformer}. */
  @SuppressWarnings("nullness:initialization.static.field.uninitialized") // set in premain()
  static Path debugPath;

  /** Print progress information, such as the arguments with which the agent is started. */
  @SuppressWarnings("WeakerAccess")
  @Option("print progress information")
  public static boolean verbose = false;

  /** The file from which to read the user replacements for replacing calls. */
  @SuppressWarnings("WeakerAccess")
  @Option("file listing methods whose calls to replace by substitute methods")
  public static @MonotonicNonNull Path replacement_file = null;

  /** Exclude transformation of classes in the listed packages. */
  @SuppressWarnings("WeakerAccess")
  @Option("file listing packages whose classes should not be transformed")
  public static @MonotonicNonNull Path dont_transform = null;

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
        String[] target_args = options.parse(true, tokenize(agentArgs));
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
      boolean createDebugDiretory = debug && debug_directory != null && !debug_directory.isEmpty();
      if (createDebugDiretory) {
        debugPath = debugPath.resolve(debug_directory);
        if (!Files.exists(debugPath)) {
          Files.createDirectory(debugPath);
        }
      }

      // Load package prefixes from the resource file in the jar for default package exclusions
      Set<String> excludedPackagePrefixes = new LinkedHashSet<>();

      String exclusionFileName = "/default-load-exclusions.txt";
      try (InputStream inputStream =
          ReplaceCallAgent.class.getResourceAsStream(exclusionFileName)) {
        if (inputStream == null) {
          replaceCallExit("Unable to find default package exclusion file.");
        }
        try {
          excludedPackagePrefixes.addAll(
              loadExclusions(new InputStreamReader(inputStream, UTF_8), exclusionFileName));
        } catch (IOException e) {
          replaceCallExit("Unable to read default package exclusion file: " + e.getMessage());
        }
      }

      // If user-provided package exclusion file, load user package exclusions
      if (dont_transform != null) {
        String dont_transform_string = dont_transform.toString();
        try (Reader dont_transform_reader = Files.newBufferedReader(dont_transform, UTF_8)) {
          excludedPackagePrefixes.addAll(
              loadExclusions(dont_transform_reader, dont_transform_string));
        } catch (IOException e) {
          // This agent has no access to RandoopUsageError and randoop.util.Util.pathAndAbsolute().
          System.err.format(
              "Error reading package exclusion file %s:%n %s%n", dont_transform, e.getMessage());
          System.exit(1); // Exit on user input error. (Throwing exception would halt JVM.)
        }
      }

      /*
       * The agent is called when classes are loaded. If Randoop is using threads, this can result
       * in multiple threads accessing the map to apply replacements.
       */
      Map<MethodSignature, MethodSignature> replacementMap = null;

      // Read the default replacement file
      String replacementPath = "/default-replacements.txt";
      try (InputStream inputStream = ReplaceCallAgent.class.getResourceAsStream(replacementPath)) {
        if (inputStream == null) {
          replaceCallExit("Unable to open default replacements file.");
        }
        try {
          replacementMap =
              ReplacementFileReader.readReplacements(
                  new InputStreamReader(inputStream, UTF_8), replacementPath);
        } catch (ReplacementFileException e) {
          replaceCallExit("Error reading default replacement file. " + e.getMessage());
        }
      }

      // If the user has provided a replacement file, load user replacements and put them into the
      // map, possibly overriding default replacements that already appear in the map.
      if (replacement_file != null) {
        try {
          replacementMap.putAll(ReplacementFileReader.readReplacements(replacement_file));
        } catch (Throwable e) {
          System.err.printf(
              "Error reading replacement file %s:%n  %s%n", replacement_file, e.getMessage());
          System.exit(1);
        }
      }

      /*
       * The flaky-filter in Randoop needs to run the generated tests using the agent with the same
       * files, but in a different working directory.  It does not use the debugging options:  each
       * test run would overwrite this run's debugging output, and Randoop interprets the test runs'
       * standard output and standard error.
       */
      MethodReplacements.setAgentPath(getAgentPath());
      MethodReplacements.setAgentArgs(createAgentArgs());

      /*
      if (debug) {
        ArrayList<MethodSignature> sortedKeys = new ArrayList<>(replacementMap.keySet());
        Collections.sort(sortedKeys);
        for (MethodSignature key : sortedKeys) {
          System.err.println("map: " + key + " : " + replacementMap.get(key));
        }
      }
      */

      // Communicate the list of replaced methods to Randoop to omit direct calls
      List<String> signatureList =
          CollectionsPlume.mapList(MethodSignature::toRawSignatureString, replacementMap.keySet());
      MethodReplacements.setReplacedMethods(signatureList);

      // Create the transformer and add to the class loader instrumentation
      CallReplacementTransformer transformer =
          new CallReplacementTransformer(replacementMap, excludedPackagePrefixes);
      transformer.addMapFileShutdownHook();
      instrumentation.addTransformer(transformer);

    } catch (Throwable e) {
      replaceCallExit(e.getMessage());
    }
  }

  /**
   * Exit the program due to an exception in the ReplaceCall agent.
   *
   * @param message the exception's message
   */
  private static void replaceCallExit(String message) {
    System.err.println("Error in replacecall agent: " + message);
    System.err.println(
        "For problems with the default replacements file, make sure that the"
            + " replacecall.jar file is on the bootclasspath.");
    System.err.println("Otherwise, please report at https://github.com/randoop/randoop/issues ,");
    System.err.println(
        "providing the information requested at"
            + " https://randoop.github.io/randoop/manual/index.html#bug-reporting .");
    System.exit(1);
  }

  /**
   * Load package names from the given file and add them to the set of excluded package names. Adds
   * a period to the end of any name that does not have one.
   *
   * @param exclusionReader the reader for the text file containing the list of excluded packages.
   *     Is closed by this method.
   * @param filename the name of the file read by the reader
   * @return the set of excluded package prefixes from the file
   * @throws IOException if there is an error reading the file
   */
  /*package-private*/ static Set<String> loadExclusions(
      @Owning Reader exclusionReader, String filename) throws IOException {
    Set<String> excludedPackagePrefixes = new LinkedHashSet<>();
    EntryFileReader.forEachEntry(
        exclusionReader,
        filename,
        (entry, lineNumber) ->
            excludedPackagePrefixes.add(entry.endsWith(".") ? entry : entry + "."));
    return excludedPackagePrefixes;
  }

  /**
   * Extracts the path for the jar of this agent from the boot classpath.
   *
   * @return the path for the jar file, as a string
   * @throws BugInAgentException if the agent is not found on the boot classpath
   */
  private static String getAgentPath() throws BugInAgentException {
    Class<?> c;
    try {
      c = Class.forName("randoop.instrument.ReplaceCallAgent");
    } catch (ClassNotFoundException | NoClassDefFoundError e) {
      throw new BugInAgentException("Error loading ReplaceCallAgent", e);
    }
    if (c.getClassLoader() != null) {
      throw new BugInAgentException("Agent should be included on bootclasspath");
    }
    String resource = "randoop/instrument/ReplaceCallAgent.class";
    URL url = ClassLoader.getSystemResource(resource);
    if (url == null) {
      // This agent has no access to RandoopUsageError and randoop.util.Util.pathAndAbsolute().
      System.err.format("Cannot find resource %s%n", resource);
      System.exit(1); // Exit on user input error. (Throwing exception would halt JVM.)
    }
    String jarFilePath = getJarPathFromURL(url);
    if (debug) {
      System.err.println("AgentPath: " + jarFilePath);
    }
    return jarFilePath;
  }

  /**
   * Extracts the path from a URL of type jar.
   *
   * @param url the url to extract the path from, such as {@code
   *     jar:file:/path/to/my%20file.jar!/path/to/MyClass.class}
   * @return the path for the jar file, as a string, such as {@code /path/to/my file.jar}
   */
  static String getJarPathFromURL(URL url) {
    // The path of a jar URL is a file URL, followed by "!/" and the entry name.
    String path = url.getPath();
    String jarFileURL = path.substring(0, path.indexOf("!/"));
    try {
      // Unlike URL.getPath(), Paths.get(URI) decodes escapes such as "%20".
      return Paths.get(new URI(jarFileURL)).toString();
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException("Malformed jar URL: " + url, e);
    }
  }

  /**
   * Creates an agent argument string that gives the current replacement file and exclusion file,
   * using absolute file names. Thus, the argument string has the same effect regardless of the
   * working directory.
   *
   * <p>The argument string omits the debugging options {@code --debug}, {@code --debug-directory},
   * and {@code --verbose}.
   *
   * @return an agent argument string that gives the current replacement file and exclusion file
   */
  static String createAgentArgs() {
    // tokenize() splits the argument string at whitespace that is not quoted.
    StringJoiner result = new StringJoiner(" ");
    if (replacement_file != null) {
      result.add("--replacement-file=" + quote(replacement_file.toAbsolutePath().toString()));
    }
    if (dont_transform != null) {
      result.add("--dont-transform=" + quote(dont_transform.toAbsolutePath().toString()));
    }
    return result.toString();
  }

  /**
   * Splits an agent argument string into arguments. Arguments are separated by whitespace. Within
   * an argument, text between a pair of single quotes or a pair of double quotes may contain
   * whitespace; the quotes themselves are removed. Where an argument contains a value quoted by
   * {@link #quote}, the result contains the original, unquoted value.
   *
   * @param args the agent argument string
   * @return the arguments in {@code args}
   */
  static String[] tokenize(String args) {
    // Options.tokenize() retains the quote characters, which is why this method removes them.
    // TODO: When upgrading to a version of plume-lib Options that includes
    // https://github.com/plume-lib/options/pull/713, whose Options.tokenize() removes the quote
    // characters, replace this method by Options.tokenize() and remove unquote().  Otherwise,
    // unquote() corrupts every value that contains a quote character.
    String[] result = Options.tokenize(args);
    for (int i = 0; i < result.length; i++) {
      // TODO: Delete this call to unquote() when upgrading to plume-lib/options#713 (see above).
      result[i] = unquote(result[i]);
    }
    return result;
  }

  /**
   * Removes the quote characters from a token returned by {@link Options#tokenize}. Text between a
   * pair of single quotes or a pair of double quotes is retained literally.
   *
   * @param token a token returned by {@link Options#tokenize}
   * @return the token, with its quote characters removed
   */
  private static String unquote(String token) {
    // TODO: Delete this method when upgrading to a version of plume-lib Options that includes
    // https://github.com/plume-lib/options/pull/713, whose Options.tokenize() removes the quote
    // characters.  Otherwise, this method corrupts every value that contains a quote character.
    StringBuilder result = new StringBuilder();
    int i = 0;
    while (i < token.length()) {
      char ch = token.charAt(i);
      if (ch == '\'' || ch == '"') {
        int close = token.indexOf(ch, i + 1);
        if (close == -1) {
          close = token.length();
        }
        result.append(token, i + 1, close);
        i = close + 1;
      } else {
        result.append(ch);
        i++;
      }
    }
    return result.toString();
  }

  /**
   * Quotes a value so that {@link #tokenize} treats it as part of a single argument and yields it
   * unchanged.
   *
   * @param value a value
   * @return {@code value}, quoted if necessary
   */
  static String quote(String value) {
    // Options.tokenize() splits at any character for which Character.isWhitespace() is true.
    if (value.chars().anyMatch(ch -> Character.isWhitespace(ch) || ch == '\'' || ch == '"')) {
      // Each single quote ends the quoted text, appears within double quotes, and resumes it.
      return "'" + value.replace("'", "'\"'\"'") + "'";
    }
    return value;
  }

  /**
   * Private exception class used to manage agent-specific errors.
   *
   * <p>Analogous to {@code RandoopBug}, but that class is not available within the agent.
   */
  private static class BugInAgentException extends Exception {

    private static final long serialVersionUID = 20250811;

    /**
     * Create a {@link BugInAgentException} with the message.
     *
     * @param message the error message
     */
    BugInAgentException(String message) {
      super(message);
    }

    /**
     * Create a {@link BugInAgentException} with the message and causing exception.
     *
     * @param message the message
     * @param cause the exception
     */
    BugInAgentException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
