package randoop.output;

import static randoop.execution.RunCommand.ProcessException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import plume.Pair;
import plume.UtilMDE;
import randoop.BugInRandoopException;
import randoop.Globals;
import randoop.compile.FileCompiler;
import randoop.compile.FileCompilerException;
import randoop.compile.SequenceClassLoader;
import randoop.execution.RunCommand;
import randoop.execution.TestEnvironment;
import randoop.main.GenTests;

/**
 * A {@link CodeWriter} that outputs JUnit tests with assertions that fail commented out. Intended
 * to be used with regression tests inorder to filter flaky tests that pass within Randoop, but fail
 * when run from the command line.
 *
 * <p>Writes the class, and then compiles and runs the tests to determine whether there are failing
 * assertions. Each failing assertion is replaced by a comment containing the code for the failing
 * assertion. Creates a clean temporary directory for each compilation/run of a test class to avoid
 * state effects due to files in the working directory.
 */
public class FailingTestFilter implements CodeWriter {

  /** A pattern matching the JUnit4 message indicating the total count of failures */
  private static final Pattern FAILURE_MESSAGE_PATTERN =
      Pattern.compile("There\\s+(?:was|were)\\s+(\\d+)\\s+failure(?:s|):");

  /** Regex for Java identifiers */
  private static final String ID_STRING =
      "\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*";

  /** The {@link randoop.execution.TestEnvironment} for running the test classes. */
  private final TestEnvironment testEnvironment;

  /** The underlying {@link randoop.output.JavaFileWriter} for writing a test class. */
  private final JavaFileWriter javaFileWriter;

  /** The class loader for the compiler */
  private final SequenceClassLoader classLoader;

  /**
   * Create a {@link FailingTestFilter} for which tests will be run in the environment and using the
   * given {@link JavaFileWriter} to output test classes.
   *
   * @param testEnvironment the {@link TestEnvironment} for executing tests during filtering
   * @param javaFileWriter the {@link JavaFileWriter} to write java files for the classes
   */
  public FailingTestFilter(TestEnvironment testEnvironment, JavaFileWriter javaFileWriter) {
    this.testEnvironment = testEnvironment;
    this.javaFileWriter = javaFileWriter;
    this.classLoader = new SequenceClassLoader(getClass().getClassLoader());
  }

  /**
   * {@inheritDoc}
   *
   * <p>Inserts comments in place of assertions that fail when the test is run.
   *
   * <p>Assumes output from JUnit4 {@code org.junit.runner.JUnitCore} runner used in {@link
   * TestEnvironment}.
   */
  @Override
  public File writeClassCode(String packageName, String classname, String classSource) {

    String qualifiedClassname = (packageName.isEmpty() ? "" : packageName + ".") + classname;

    int pass = 0; // Used to create unique working directory name.
    boolean passing = false;

    while (!passing) {
      Path workingDirectory = createWorkingDirectory(classname, pass);

      compileTestClass(packageName, classname, classSource, workingDirectory);

      RunCommand.Status status;
      try {
        status = testEnvironment.runTest(qualifiedClassname, workingDirectory.toFile());
      } catch (ProcessException e) {
        throw new BugInRandoopException("Error filtering regression tests", e);
      }

      if (status.exitStatus == 0) {
        passing = true;
      } else {
        classSource = editSource(classname, classSource, status);
      }
      pass++;
    }
    return javaFileWriter.writeClassCode(packageName, classname, classSource);
  }

  @Override
  public File writeUnmodifiedClassLines(
      String packageName, String classname, String[] sourceLines) {
    return javaFileWriter.writeUnmodifiedClassLines(packageName, classname, sourceLines);
  }

  @Override
  public File writeUnmodifiedClassCode(String packageName, String classname, String classCode) {
    return javaFileWriter.writeClassCode(packageName, classname, classCode);
  }

  /**
   * Use the failures in the {@code status} from running JUnit with {@code classCode} to identify
   * lines with failing assertions and replaces them with a line comment with the assertion text
   *
   * @param classname the name of the test class
   * @param classCode the source code for the test class, each assertion must be on its own line
   * @param status the {@link RunCommand.Status} for running the test with JUnit
   * @return the class source edited so that failing assertions are replaced by line comments
   * @throws BugInRandoopException if {@code status} contains output for a failure not involving a
   *     Randoop generated test method
   */
  private String editSource(String classname, String classCode, RunCommand.Status status) {
    // JUnit4 writes to standard out, check this doesn't change.
    assert status.errorOutputLines.isEmpty()
        : "Expecting JUnit to write to standard out, but found output on standard error";

    /* Iterator to move through JUnit output. */
    Iterator<String> lineIterator = status.standardOutputLines.iterator();

    /*
     * First, find the message that indicates the number of failures in the run.
     */

    Pair<String, String> failureCountMatch;
    try {
      failureCountMatch = readUntilMatch(lineIterator, FAILURE_MESSAGE_PATTERN);
    } catch (JUnitReaderException e) {
      throw new BugInRandoopException(
          "Error reading JUnit output: no match for failure count message");
    }
    int totalFailures = Integer.parseInt(failureCountMatch.b);
    assert totalFailures > 0 : "JUnit has non-zero exit status, but no failure found";

    /*
     * Then read the rest of the file to find each failure.
     * The standard runner gives a numbered list with each entry matching the following pattern:
     */
    Pattern failureHeaderPattern =
        Pattern.compile("\\d+\\)\\s+(" + ID_STRING + ")\\(" + ID_STRING + "\\)");

    /*
     * Split class text so that we can match the line number for the assertion with the code.
     * Use same line break as used to write test class file.
     */
    String[] classLines = classCode.split(Globals.lineSep);

    int failureCount = 0;
    while (failureCount < totalFailures) {
      /*
       * Read until beginning of failure
       */
      Pair<String, String> failureHeaderMatch;
      try {
        failureHeaderMatch = readUntilMatch(lineIterator, failureHeaderPattern);
      } catch (JUnitReaderException e) {
        throw new BugInRandoopException("Error reading JUnit output: no match for failure header");
      }
      String methodName = failureHeaderMatch.b;
      String line = failureHeaderMatch.a;

      /*
       * If the method name in the failure message is not a test method, throw an exception.
       */
      if (!methodName.matches(GenTests.TEST_METHOD_NAME_PREFIX + "\\d+")) {
        if (line.contains("initializationError")) {
          throw new BugInRandoopException(
              "Check configuration of test environment: "
                  + "initialization error of test in flaky-test filter");
        } else {
          throw new BugInRandoopException("Unexpected failure in flaky-test filter: " + methodName);
        }
      }

      /*
       * Search for the stacktrace entry corresponding to the test method, and capture the line
       * number.
       */
      Pattern linePattern =
          Pattern.compile(
              "\\s+at\\s+"
                  + classname
                  + "\\."
                  + methodName
                  + "\\("
                  + classname
                  + "\\.java:(\\d+)\\)");

      Pair<String, String> failureLineMatch;
      try {
        failureLineMatch = readUntilMatch(lineIterator, linePattern);
      } catch (JUnitReaderException e) {
        throw new BugInRandoopException(
            "Error reading JUnit output: no match for failure in stacktrace");
      }
      int lineNumber = Integer.parseInt(failureLineMatch.b);
      if (lineNumber < 1 && lineNumber < classLines.length + 1) {
        throw new BugInRandoopException(
            "Line number "
                + lineNumber
                + " read from JUnit out of range [1,"
                + (classLines.length + 1)
                + "]");
      }
      classLines[lineNumber - 1] = "// flaky: " + classLines[lineNumber - 1];

      failureCount++;
    }

    //XXX have this method return the array and redo writeClass so that it writes from array (?)
    return UtilMDE.join(classLines, Globals.lineSep);
  }

  /**
   * Read lines of the JUnit output using the iterator until finding a match for the pattern, and
   * then returns a pair containing the line and the text matching the first group of the pattern.
   * Assumes that there is a match, and that the pattern has at least one group.
   *
   * @param lineIterator the iterator for reading from the JUnit output
   * @param pattern the pattern for a regex with at least one group
   * @return the pair containing the line and the text matching the first group
   * @throws JUnitReaderException if the iterator has no more lines, but the pattern hasn't been
   *     matched
   */
  private Pair<String, String> readUntilMatch(Iterator<String> lineIterator, Pattern pattern)
      throws JUnitReaderException {
    while (lineIterator.hasNext()) {
      String line = lineIterator.next();
      Matcher matcher = pattern.matcher(line);
      if (matcher.matches()) {
        return Pair.of(line, matcher.group(1));
      }
    }
    throw new JUnitReaderException("Error JUnit output doesn't match expected format");
  }

  /**
   * Compiles the Java files in the list of files and writes the resulting class files to the
   * directory.
   *
   * <p>Calls {@code System.exit()} if the file does not compile.
   *
   * @param packageName the package name for the test class
   * @param classname the name of the test class
   * @param classSource the text of the test class
   * @param destinationDir the directory for class file output
   */
  private void compileTestClass(
      String packageName, String classname, String classSource, Path destinationDir) {
    /*
     * The use of FileCompiler is temporary. Should be replaced by use of SequenceCompiler, which
     * will compile from source, once it is able to write the class file to disk.
     */
    List<File> sourceFiles = new ArrayList<>();
    sourceFiles.add(javaFileWriter.writeClassCode(packageName, classname, classSource));
    FileCompiler fileCompiler = new FileCompiler();
    try {
      fileCompiler.compile(sourceFiles, destinationDir);
    } catch (FileCompilerException e) {
      throw new BugInRandoopException("Compilation error during flaky-test filtering", e);
    }
  }

  /**
   * Creates a temporary directory using the class name and a pass count to form the directory name.
   *
   * @param classname the class name used to form the temporary directory name
   * @param pass the pass count used to form the temporary directory name
   * @return the {@code Path} for the directory created
   */
  private Path createWorkingDirectory(String classname, int pass) {
    Path workingDirectory = null;
    try {
      workingDirectory = Files.createTempDirectory("check" + classname + pass);
      //workingDirectory.toFile().deleteOnExit();
    } catch (IOException e) {
      System.err.printf(
          "Unable to create temporary directory for flaky-test filtering, exception: %s%n",
          e.getMessage());
      System.exit(1);
    }
    return workingDirectory;
  }

  /** Exception to indicate an error when reading the JUnit output. */
  private static class JUnitReaderException extends Throwable {
    private static final long serialVersionUID = 7160657703477853170L;

    /**
     * Create a {@link JUnitReaderException} with the given message.
     *
     * @param message the exception message
     */
    JUnitReaderException(String message) {
      super(message);
    }
  }
}
