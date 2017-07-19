package randoop.output;

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
import randoop.compile.FileCompilerException;
import randoop.execution.ProcessException;
import randoop.execution.RunStatus;
import randoop.execution.TestEnvironment;
import randoop.main.GenTests;

/**
 * A {@link CodeWriter} that outputs JUnit tests with assertions that fail commented out. Intended
 * to be run on regression tests to filter tests that pass within Randoop, but fail when run from
 * the command line.
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

  /**
   * Create a {@link FailingTestFilter} run in the environment and using the given {@link
   * JavaFileWriter} to output test classes.
   *
   * @param testEnvironment the {@link TestEnvironment} for executing tests during filtering
   * @param javaFileWriter the {@link JavaFileWriter} to write java files for the classes
   */
  public FailingTestFilter(TestEnvironment testEnvironment, JavaFileWriter javaFileWriter) {
    this.testEnvironment = testEnvironment;
    this.javaFileWriter = javaFileWriter;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Inserts comments in place of assertions that fail when the test is run.
   *
   * <p>May be sensitive to changes in JUnit runner output.
   */
  @Override
  public File writeClass(String packageName, String classname, String classString) {

    List<File> sourceList = new ArrayList<>();
    File testFile = javaFileWriter.writeClass(packageName, classname, classString);
    sourceList.add(testFile);

    String qualifiedClassname = (packageName.isEmpty() ? "" : packageName + ".") + classname;

    int pass = 0;
    boolean passing = false;

    while (!passing) {

      Path workingDirectory = createWorkingDirectory(classname, pass);

      boolean success = compileTestClass(sourceList, workingDirectory);
      assert success;

      RunStatus status = null;
      try {
        status = testEnvironment.runTest(qualifiedClassname, workingDirectory.toFile());
      } catch (ProcessException e) {
        throw new BugInRandoopException("Error filtering regression tests: " + e.getMessage());
      }
      if (status.exitStatus == 0) {
        passing = true;
        continue;
      }

      /*
       * Read the standard output from running the test class using JUnit4.
       */
      assert status.errorOutputLines.isEmpty()
          : "JUnit writes to standard out, error output unexpected";

      /*
       * First, find the message that indicates the number of failures in the run.
       */
      int totalFailures = -1;
      Iterator<String> lineIterator = status.standardOutputLines.iterator();
      while (lineIterator.hasNext() && totalFailures < 0) {
        String line = lineIterator.next();
        Matcher messageMatcher = FAILURE_MESSAGE_PATTERN.matcher(line);
        if (messageMatcher.matches()) {
          totalFailures = Integer.parseInt(messageMatcher.group(1));
        }
      }
      assert lineIterator.hasNext()
          : "JUnit has non-zero exit status, but we didn't find a failure";

      /*
       * Then read the rest of the file to find each failure.
       * The standard runner gives a numbered list with each entry matching the following pattern:
       */
      Pattern failureHeaderPattern =
          Pattern.compile("\\d+\\)\\s+(" + ID_STRING + ")\\(" + classname + "\\)");

      // Split the class text string so that we can match the line number for the assertion with the code.
      String[] classLines =
          classString.split(Globals.lineSep); //use same line break as used to write file

      int failureCount = 0;
      while (lineIterator.hasNext() && failureCount < totalFailures) {
        String methodName;
        int lineNumber;
        String line = lineIterator.next();
        Matcher failureMatcher = failureHeaderPattern.matcher(line);
        if (failureMatcher.matches()) { // found the beginning of a failure
          failureCount++;
          methodName = failureMatcher.group(1);

          /*
           * If the method name in the failure message is not a test method, throw an exception.
           */
          if (!methodName.matches(GenTests.TEST_METHOD_NAME_PREFIX + "\\d+")) {
            if (line.contains("initializationError")) {
              throw new BugInRandoopException(
                  "Check configuration of test environment: "
                      + "initialization error of test in flaky-test filter");
            } else {
              throw new BugInRandoopException(
                  "Unexpected failure in flaky-test filter: " + methodName);
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
          lineNumber = -1;
          while (lineIterator.hasNext() && lineNumber < 0) {
            line = lineIterator.next();
            Matcher lineMatcher = linePattern.matcher(line);
            if (lineMatcher.matches()) {
              lineNumber = Integer.parseInt(lineMatcher.group(1));
            }
          }
          assert lineNumber - 1 < classLines.length;
          classLines[lineNumber - 1] = "// flaky: " + classLines[lineNumber - 1];
        }
      }

      classString = UtilMDE.join(classLines, Globals.lineSep);
      testFile = javaFileWriter.writeClass(packageName, classname, classString);
      pass++;
    }

    return testFile;
  }

  /**
   * Compiles the Java files in the list of files and writes the resulting class files to the
   * directory.
   *
   * <p>Calls {@code System.exit()} if the file does not compile.
   *
   * @param sourceList the list of source files
   * @param outputDirectory the directory for class file output
   * @return true if the classes compile without error
   */
  private boolean compileTestClass(List<File> sourceList, Path outputDirectory) {
    boolean success = false;
    FileCompiler compiler = new FileCompiler();
    try {
      success = compiler.compile(sourceList, outputDirectory);
    } catch (FileCompilerException e) {
      System.err.printf("Compilation error during flaky-test filtering: " + e.getMessage());
      //XXX need better messages here
      System.exit(1);
    }
    return success;
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
      workingDirectory.toFile().deleteOnExit();
    } catch (IOException e) {
      System.err.printf(
          "Unable to create temporary directory for flaky-test filtering, exception: %s%n",
          e.getMessage());
      System.exit(1);
    }
    return workingDirectory;
  }

  @Override
  public File writeUnmodifiedClass(String packageName, String classname, String classString) {
    return javaFileWriter.writeClass(packageName, classname, classString);
  }
}
