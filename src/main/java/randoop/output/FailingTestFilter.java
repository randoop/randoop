package randoop.output;

import static randoop.execution.RunCommand.CommandException;
import static randoop.reflection.SignatureParser.DOT_DELIMITED_IDS;
import static randoop.reflection.SignatureParser.ID_STRING;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import plume.UtilMDE;
import randoop.BugInRandoopException;
import randoop.Globals;
import randoop.compile.FileCompiler;
import randoop.execution.RunCommand;
import randoop.execution.TestEnvironment;
import randoop.main.GenTests;

/**
 * A {@link CodeWriter} that outputs JUnit tests with assertions that fail commented out. Intended
 * to be used with regression tests in order to filter flaky tests that pass within Randoop, but
 * fail when run from the command line.
 *
 * <p>Writes the class, and then compiles and runs the tests to determine whether there are failing
 * assertions. Each failing assertion is replaced by a comment containing the code for the failing
 * assertion. Creates a clean temporary directory for each compilation/run of a test class to avoid
 * state effects due to files in the working directory.
 */
public class FailingTestFilter implements CodeWriter {

  /**
   * A pattern matching the JUnit4 message indicating the total count of failures. Capturing group 1
   * is the number of failures.
   */
  private static final Pattern FAILURE_MESSAGE_PATTERN =
      Pattern.compile("There\\s+(?:was|were)\\s+(\\d+)\\s+failure(?:s|):");

  private static final Pattern FAILURE_HEADER_PATTERN =
      Pattern.compile("\\d+\\)\\s+(" + ID_STRING + ")\\(" + DOT_DELIMITED_IDS + "\\)");

  /** The {@link randoop.execution.TestEnvironment} for running the test classes. */
  private final TestEnvironment testEnvironment;

  /** The underlying {@link randoop.output.JavaFileWriter} for writing a test class. */
  private final JavaFileWriter javaFileWriter;

  /**
   * Create a {@link FailingTestFilter} for which tests will be run in the environment and which
   * uses the given {@link JavaFileWriter} to output test classes.
   *
   * @param testEnvironment the {@link TestEnvironment} for executing tests during filtering
   * @param javaFileWriter the {@link JavaFileWriter} to write {@code .java} files for the classes
   */
  public FailingTestFilter(TestEnvironment testEnvironment, JavaFileWriter javaFileWriter) {
    this.testEnvironment = testEnvironment;
    this.javaFileWriter = javaFileWriter;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Replaces failing assertions by comments.
   *
   * <p>Assumes output from JUnit4 {@code org.junit.runner.JUnitCore} runner used in {@link
   * TestEnvironment}.
   */
  @Override
  public File writeClassCode(String packageName, String classname, String classSource)
      throws RandoopOutputException {

    String qualifiedClassname = (packageName.isEmpty() ? "" : packageName + ".") + classname;

    int pass = 0; // Used to create unique working directory name.
    boolean passing = false;

    while (!passing) {
      Path workingDirectory = createWorkingDirectory(classname, pass);

      compileTestClass(packageName, classname, classSource, workingDirectory);

      RunCommand.Status status;
      try {
        status = testEnvironment.runTest(qualifiedClassname, workingDirectory.toFile());
      } catch (CommandException e) {
        throw new BugInRandoopException("Error filtering regression tests", e);
      }

      if (status.exitStatus == 0 && !status.timedOut) {
        passing = true;
      } else {
        classSource = commentFailingAssertions(packageName, classname, classSource, status);
      }
      pass++;
    }
    return javaFileWriter.writeClassCode(packageName, classname, classSource);
  }

  @Override
  public File writeUnmodifiedClassCode(String packageName, String classname, String javaCode)
      throws RandoopOutputException {
    return javaFileWriter.writeClassCode(packageName, classname, javaCode);
  }

  /**
   * Comments out lines with failing assertions. Uses the failures in the {@code status} from
   * running JUnit with {@code javaCode} to identify lines with failing assertions.
   *
   * @param packageName the package name of the test class
   * @param classname the name of the test class
   * @param javaCode the source code for the test class, each assertion must be on its own line
   * @param status the {@link randoop.execution.RunCommand.Status} for running the test with JUnit
   * @return the class source edited so that failing assertions are replaced by comments
   * @throws BugInRandoopException if {@code status} contains output for a failure not involving a
   *     Randoop-generated test method
   */
  private String commentFailingAssertions(
      String packageName, String classname, String javaCode, RunCommand.Status status) {

    /* Iterator to move through JUnit output. (JUnit only writes to standard output.) */
    Iterator<String> lineIterator = status.standardOutputLines.iterator();

    /*
     * First, find the message that indicates the number of failures in the run.
     */
    Match failureCountMatch = readUntilMatch(lineIterator, FAILURE_MESSAGE_PATTERN);
    int totalFailures = Integer.parseInt(failureCountMatch.group);
    if (totalFailures < 0) {
      throw new BugInRandoopException("JUnit has non-zero exit status, but no failure found");
    }

    /*
     * Then read the rest of the file to find each failure.
     */

    /*
     * Split Java code text so that we can match the line number for the assertion with the code.
     * Use same line break as used to write test class file.
     */
    String[] javaCodeLines = javaCode.split(Globals.lineSep);

    for (int failureCount = 0; failureCount < totalFailures; failureCount++) {
      /*
       * Read until beginning of failure
       */
      Match failureHeaderMatch = readUntilMatch(lineIterator, FAILURE_HEADER_PATTERN);
      String line = failureHeaderMatch.line;
      String methodName = failureHeaderMatch.group;

      /*
       * If the method name in the failure message is not a test method, throw an exception.
       */
      if (!methodName.matches(GenTests.TEST_METHOD_NAME_PREFIX + "\\d+")) {
        if (line.contains("initializationError")) {
          throw new BugInRandoopException(
              "Check configuration of test environment: "
                  + "initialization error of test in flaky-test filter: "
                  + line);
        } else {
          throw new BugInRandoopException(
              "Bad method name " + methodName + " in flaky-test filter: " + line);
        }
      }

      /*
       * Search for the stacktrace entry corresponding to the test method, and capture the line
       * number.
       */
      String qualifiedClassname = ((packageName.isEmpty()) ? "" : packageName + ".") + classname;
      Pattern linePattern =
          Pattern.compile(
              String.format(
                  "\\s+at\\s+%s\\.%s\\(%s\\.java:(\\d+)\\)",
                  qualifiedClassname, methodName, classname));

      Match failureLineMatch = readUntilMatch(lineIterator, linePattern);
      int lineNumber = Integer.parseInt(failureLineMatch.group);
      if (lineNumber < 1 || lineNumber > javaCodeLines.length) {
        throw new BugInRandoopException(
            "Line number "
                + lineNumber
                + " read from JUnit out of range [1,"
                + (javaCodeLines.length + 1)
                + "]: "
                + failureLineMatch.line);
      }
      javaCodeLines[lineNumber - 1] = "// flaky: " + javaCodeLines[lineNumber - 1];
    }

    //XXX For efficiency, have this method return the array and redo writeClass so that it writes from array (?).
    return UtilMDE.join(javaCodeLines, Globals.lineSep);
  }

  /**
   * Reads lines of the JUnit output using the iterator until finding a match for the pattern, and
   * then returns a pair containing the line and the text matching the first group of the pattern.
   * Assumes that there is a match, and that the pattern has at least one group.
   *
   * @param lineIterator the iterator for reading from the JUnit output
   * @param pattern the pattern for a regex with at least one group
   * @return the pair containing the line and the text matching the first group
   * @throws BugInRandoopException if the iterator has no more lines, but the pattern hasn't been
   *     matched
   */
  private Match readUntilMatch(Iterator<String> lineIterator, Pattern pattern) {
    while (lineIterator.hasNext()) {
      String line = lineIterator.next();
      Matcher matcher = pattern.matcher(line);
      if (matcher.matches()) {
        return new Match(line, matcher.group(1));
      }
    }
    throw new BugInRandoopException("Error: JUnit output doesn't contain: " + pattern.pattern());
  }

  /**
   * Compiles the Java files in the list of files and writes the resulting class files to the
   * directory.
   *
   * @param packageName the package name for the test class
   * @param classname the name of the test class
   * @param classSource the text of the test class
   * @param destinationDir the directory for class file output
   * @throws BugInRandoopException if the file does not compile
   */
  private void compileTestClass(
      String packageName, String classname, String classSource, Path destinationDir) {
    // TODO: The use of FileCompiler is temporary. Should be replaced by use of SequenceCompiler,
    // which will compile from source, once it is able to write the class file to disk.
    List<File> sourceFiles = new ArrayList<>();
    try {
      sourceFiles.add(javaFileWriter.writeClassCode(packageName, classname, classSource));
    } catch (RandoopOutputException e) {
      throw new BugInRandoopException("Output error during flaky-test filtering", e);
    }
    FileCompiler fileCompiler = new FileCompiler();
    try {
      fileCompiler.compile(sourceFiles, destinationDir);
    } catch (FileCompiler.FileCompilerException e) {
      throw new BugInRandoopException("Compilation error during flaky-test filtering", e);
    }
  }

  /**
   * Creates a temporary directory by concatenating the class name and a pass count to form the
   * directory name.
   *
   * @param classname the class name
   * @param pass the pass count
   * @return the {@code Path} for the directory created
   */
  private Path createWorkingDirectory(String classname, int pass) {
    try {
      Path workingDirectory = Files.createTempDirectory("check" + classname + pass);
      workingDirectory.toFile().deleteOnExit();
      return workingDirectory;
    } catch (IOException e) {
      // not BugInRandoopException
      System.err.printf(
          "Unable to create temporary directory for flaky-test filtering, exception: %s%n",
          e.getMessage());
      System.exit(1);
      throw new Error("unreachable statement");
    }
  }

  /** The line and first group from the match of a {@code Pattern}. */
  private static class Match {

    /** The line that matched the pattern. */
    final String line;

    /** The substring that matched the group. */
    final String group;

    /**
     * Creates a {@link Match} record with the given line and group.
     *
     * @param line the matched line
     * @param group the matched group substring
     */
    Match(String line, String group) {
      this.line = line;
      this.group = group;
    }
  }
}
