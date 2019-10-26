package randoop.main;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * Captures the status of a Randoop run, along with status from the compilation of the
 * Randoop-generated tests.
 */
class RandoopRunStatus {

  /** The {@link ProcessStatus} for the Randoop run. */
  final ProcessStatus processStatus;

  /** The number of operators used in the Randoop run. */
  final int operatorCount;

  /** The number of generated regression tests. */
  final int regressionTestCount;

  /** The number of generated error-revealing tests. */
  final int errorTestCount;

  /**
   * The top suspected "flaky" nondeterministic methods to output. The size is no greater than
   * {@code randoop.main.GenInputsAbstract#nondeterministic_methods_to_output}.
   */
  final List<String> suspectedFlakyMethodNames;

  /** Is output to the user before each possibly flaky method. */
  public static final String POSSIBLY_FLAKY_PREFIX = "  Possibly flaky:  ";

  /**
   * Creates a {@link RandoopRunStatus} object with the given {@link ProcessStatus}, operator count,
   * generated test counts, and suspected flaky method names.
   *
   * @param processStatus the status of Randoop execution
   * @param operatorCount the number of operators used in generation
   * @param regressionTestCount the number of generated regression tests
   * @param errorTestCount the number of generated error-revealing tests
   * @param suspectedFlakyMethodNames the suspected flaky methods (in descending order of tf-idf)
   */
  private RandoopRunStatus(
      ProcessStatus processStatus,
      int operatorCount,
      int regressionTestCount,
      int errorTestCount,
      List<String> suspectedFlakyMethodNames) {
    this.processStatus = processStatus;
    this.operatorCount = operatorCount;
    this.regressionTestCount = regressionTestCount;
    this.errorTestCount = errorTestCount;
    this.suspectedFlakyMethodNames = suspectedFlakyMethodNames;
  }

  /**
   * Runs Randoop.
   *
   * <p>Should only be called if a test only runs Randoop.
   *
   * @param testEnvironment the {@link SystemTestEnvironment} for this run
   * @param options the command-line arguments to Randoop
   * @return the status information collected from generation
   */
  static ProcessStatus generate(SystemTestEnvironment testEnvironment, RandoopOptions options) {
    List<String> command = new ArrayList<>();
    command.add("java");
    command.add("-ea");
    // cannot use randoop.main.GenInputsAbstract.jvm_max_memory due to package clash
    command.add("-Xmx3000m");
    command.add("-XX:+HeapDumpOnOutOfMemoryError");
    if (testEnvironment.getBootClassPath() != null
        && !testEnvironment.getBootClassPath().isEmpty()) {
      command.add("-Xbootclasspath/a:" + testEnvironment.getBootClassPath());
    }

    command.add("-classpath");
    // This version can make a command that is too long (over 4096 characters).
    command.add(testEnvironment.getSystemTestClasspath());
    // In Java 9+, use a Java "argument file":
    // String classpathFilename = testEnvironment.workingDir + "filename.txt";
    // try (PrintWriter out = new PrintWriter(classpathFilename)) {
    //   out.println(testEnvironment.getSystemTestClasspath());
    // } catch (FileNotFoundException e) {
    //   e.printStackTrace();
    //   System.exit(1);
    // }
    // command.add("@" + classpathFilename);

    if (testEnvironment.getJavaAgentPath() != null) {
      String agent = "-javaagent:" + testEnvironment.getJavaAgentPath();
      String args = testEnvironment.getJavaAgentArgumentString();
      if (args != null) {
        agent = agent + "=" + args;
      }
      command.add(agent);
    }
    command.add("randoop.main.Main");
    command.add("gentests");
    command.addAll(options.getOptions());
    System.out.format("RandoopRunStatus.generate() command:%n%s%n", command);
    return ProcessStatus.runCommand(command);
  }

  /**
   * Runs Randoop and compiles.
   *
   * @param testEnvironment the {@link SystemTestEnvironment} for this run
   * @param options the command-line arguments to Randoop
   * @return the status information collected from generation and compilation
   */
  static RandoopRunStatus generateAndCompile(
      SystemTestEnvironment testEnvironment, RandoopOptions options, boolean allowRandoopFailure) {

    /// Generate tests.

    ProcessStatus randoopExitStatus = generate(testEnvironment, options);

    if (randoopExitStatus.exitStatus != 0) {
      if (allowRandoopFailure) {
        return getRandoopRunStatus(randoopExitStatus);
      } else {
        System.out.println(randoopExitStatus.dump());
        fail(
            String.format(
                "Test generation exited with %d exit status, see process status details above.",
                randoopExitStatus.exitStatus));
      }
    }

    /// Check that test files are there.

    String packageName = options.getPackageName();
    String packagePathString = packageName == null ? "" : packageName.replace('.', '/');
    String regressionBasename = options.getRegressionBasename();
    String errorBasename = options.getErrorBasename();

    // determine whether files are really there and have the right names
    Path srcDir = testEnvironment.sourceDir.resolve(packagePathString);
    List<File> testSourceFiles = getFiles(srcDir, "*.java", regressionBasename, errorBasename);

    // Definitely cannot do anything useful if no generated test files
    // but not sure that this is the right way to deal with it.
    // What if test is meant not to generate anything ?
    if (testSourceFiles.size() == 0) {
      for (String line : randoopExitStatus.outputLines) {
        System.err.println(line);
      }
      fail("No test class source files found");
    }

    /// Compile.

    Path classDir = testEnvironment.classDir;
    CompilationStatus compileStatus =
        CompilationStatus.compileTests(
            testSourceFiles, testEnvironment.getSystemTestClasspath(), classDir.toString());
    if (!compileStatus.succeeded) {
      System.out.println("Compilation: ");
      if (randoopExitStatus.exitStatus == 0) {
        for (String line : randoopExitStatus.outputLines) {
          System.err.println(line);
        }
      }
      compileStatus.printDiagnostics(System.err);

      fail("Compilation failed");
    }

    Path classFileDir = classDir.resolve(packagePathString);
    List<File> testClassFiles =
        getFiles(classFileDir, "*.class", regressionBasename, errorBasename);
    assertEquals(
        "Number of compiled test files must equal source test files",
        testSourceFiles.size(),
        testClassFiles.size());

    // Compilation succeeded.  Return the result of test generation.
    return getRandoopRunStatus(randoopExitStatus);
  }

  /**
   * Collects the list of {@code Path} objects for the files in the given directory that have the
   * extension. If the directory contains any files that do not begin with one of the given
   * basenames, then fails the calling test.
   *
   * @param dirPath the directory path
   * @param extension the expected file extension
   * @param regressionBasename the basename for regression tests
   * @param errorBasename the basename for error-revealing tests
   * @return the list of tests with the extension in the directory
   */
  private static List<File> getFiles(
      Path dirPath, String extension, String regressionBasename, String errorBasename) {
    List<File> files = new ArrayList<>();
    try (DirectoryStream<Path> stream = Files.newDirectoryStream(dirPath, extension)) {
      for (Path entry : stream) {
        String filename = entry.getFileName().toString();
        assertThat(
            "Test class filename should start with basename",
            filename,
            is(anyOf(startsWith(regressionBasename), startsWith(errorBasename))));
        files.add(entry.toFile());
      }
    } catch (IOException e) {
      fail("Exception reading working directory " + e);
    }
    return files;
  }

  /**
   * Converts the {@link ProcessStatus} of a Randoop run to a {@link RandoopRunStatus} object.
   * Counts the numbers of operators collected, regression tests generated, and error-revealing
   * tests generated.
   *
   * @param ps the {@link ProcessStatus} from a Randoop run
   * @return the {@link RandoopRunStatus} for the given process status
   */
  private static RandoopRunStatus getRandoopRunStatus(ProcessStatus ps) {
    int operatorCount = 0;
    int regressionTestCount = 0;
    int errorTestCount = 0;
    List<String> suspectedFlakyMethodNames = new ArrayList<>();

    for (String line : ps.outputLines) {
      if (line.startsWith(POSSIBLY_FLAKY_PREFIX)) {
        suspectedFlakyMethodNames.add(line.substring(POSSIBLY_FLAKY_PREFIX.length()));
      } else if (line.contains("PUBLIC MEMBERS=") || line.contains("test count:")) {
        int count = Integer.valueOf(line.replaceFirst("\\D*(\\d*).*", "$1"));
        if (line.contains("PUBLIC MEMBERS=")) {
          operatorCount = count;
        } else if (line.contains("Regression")) {
          regressionTestCount = count;
        } else if (line.contains("Error")) {
          errorTestCount = count;
        }
      }
    }

    return new RandoopRunStatus(
        ps, operatorCount, regressionTestCount, errorTestCount, suspectedFlakyMethodNames);
  }

  @Override
  public String toString() {
    StringJoiner sb = new StringJoiner(System.lineSeparator());
    sb.add("RandoopRunStatus:");
    sb.add(String.format(" processStatus %s", processStatus));
    sb.add(String.format("  operatorCount %s", operatorCount));
    sb.add(String.format("  regressionTestCount %s", regressionTestCount));
    sb.add(String.format("  errorTestCount %s", errorTestCount));
    return sb.toString();
  }
}
