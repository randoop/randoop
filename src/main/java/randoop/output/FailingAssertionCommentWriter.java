package randoop.output;

import static randoop.execution.RunCommand.CommandException;
import static randoop.execution.RunCommand.Status;
import static randoop.main.GenInputsAbstract.FlakyTestAction;
import static randoop.reflection.SignatureParser.DOT_DELIMITED_IDS;
import static randoop.reflection.SignatureParser.ID_STRING;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import org.plumelib.util.FilesPlume;
import org.plumelib.util.StringsPlume;
import randoop.Globals;
import randoop.compile.FileCompiler;
import randoop.execution.TestEnvironment;
import randoop.generation.AbstractGenerator;
import randoop.main.GenInputsAbstract;
import randoop.main.GenTests;
import randoop.main.RandoopBug;
import randoop.main.RandoopUsageError;

/**
 * A {@link CodeWriter} that comments out failing assertions when outputting JUnit tests. This
 * disables flaky tests that pass when run reflectively within Randoop, but fail when run from the
 * command line. The whole test is not commented or removed, because the test might have side
 * effects that later tests depend upon.
 *
 * <p>Iteratively does the following:
 *
 * <ul>
 *   <li>Writes the class.
 *   <li>Compiles and runs the tests to determine whether there are failing assertions.
 *   <li>Replaces each failing assertion by a comment containing the code for the failing assertion.
 * </ul>
 *
 * Creates a clean temporary directory for each compilation/run of a test class to avoid state
 * effects due to files in the working directory.
 */
public class FailingAssertionCommentWriter implements CodeWriter {

  /**
   * A pattern matching the JUnit4 message indicating the total count of failures. Capturing group 1
   * is the number of failures.
   */
  private static final Pattern FAILURE_MESSAGE_PATTERN =
      Pattern.compile("There\\s+(?:was|were)\\s+(\\d+)\\s+failure(?:s|):");

  private static final Pattern FAILURE_HEADER_PATTERN =
      Pattern.compile("\\d+\\)\\s+(" + ID_STRING + ")\\(" + DOT_DELIMITED_IDS + "\\)");

  /** Matches a type: a class name, optional generics, optional array brackets. */
  private static final String TYPE_REGEX =
      randoop.instrument.ReplacementFileReader.DOT_DELIMITED_IDS + "(?:<[^=;]*>)?" + "(?:\\[\\])*";

  /**
   * Matches a variable declaration. Capturing group 1 is through the "=", 2 is the type, 3 is the
   * initializer.
   */
  private static final Pattern VARIABLE_DECLARATION_LINE =
      Pattern.compile(
          "^([ \t]*"
              + ("(" + TYPE_REGEX + ")")
              + "[ \t]+"
              + randoop.instrument.ReplacementFileReader.ID_STRING
              + "[ \t]*=[ \t]*)(.*)$");

  /** The {@link randoop.execution.TestEnvironment} for running the test classes. */
  private final TestEnvironment testEnvironment;

  /** The underlying {@link randoop.output.JavaFileWriter} for writing a test class. */
  private final JavaFileWriter javaFileWriter;

  /** Method names for flaky tests (e.g., "test005"). */
  private final HashSet<String> flakyTestNames = new HashSet<>();

  /**
   * Create a {@link FailingAssertionCommentWriter}.
   *
   * @param testEnvironment the {@link TestEnvironment} for executing tests during filtering
   * @param javaFileWriter the {@link JavaFileWriter} to write {@code .java} files for the classes
   */
  public FailingAssertionCommentWriter(
      TestEnvironment testEnvironment, JavaFileWriter javaFileWriter) {
    this.testEnvironment = testEnvironment;
    this.javaFileWriter = javaFileWriter;
  }

  /**
   * Returns the set of flaky test names. Each element has the form testNNN where N are digits; for
   * example, "test002".
   *
   * @return the flaky test names
   */
  public TreeSet<String> getFlakyTestNames() {
    return new TreeSet<>(flakyTestNames);
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
  public Path writeClassCode(String packageName, String classname, String classSource)
      throws RandoopOutputException {
    assert !Objects.equals(packageName, "");

    String qualifiedClassname = packageName == null ? classname : packageName + "." + classname;

    int iteration = 0; // Used to create unique working directory name.
    boolean passing = false; // true if all tests pass

    while (!passing) {
      Path workingDirectory = createWorkingDirectory(classname, iteration);
      try {

        // Compile

        try {
          compileTestClass(packageName, classname, classSource, workingDirectory);
        } catch (FileCompiler.FileCompilerException e) {
          classSource =
              commentCatchStatements(
                  packageName,
                  classSource,
                  e.getDiagnostics().getDiagnostics(),
                  workingDirectory,
                  e);
          continue;
        }

        // Run tests

        Status status;
        try {
          status = testEnvironment.runTest(qualifiedClassname, workingDirectory);
        } catch (CommandException e) {
          throw new RandoopBug("Error filtering regression tests", e);
        }

        if (status.exitStatus == 0) {
          passing = true;
        } else if (status.timedOut) {
          throw new Error("runTest timed out for class " + qualifiedClassname + ": " + status);
        } else if (status.exitStatus == 137) {
          System.out.printf(
              "runTest exit status 137 in FailingAssertionCommentWriter.writeClassCode(%s, %s)%n",
              packageName, classname);
          System.out.printf("classSource:%n");
          System.out.println(classSource);
          throw new Error(
              "runTest exit status 137 for class "
                  + qualifiedClassname
                  + ": "
                  + status
                  + "classSource: "
                  + classSource);
        } else {
          classSource =
              commentFailingAssertions(packageName, classname, classSource, status, flakyTestNames);
        }
      } finally {
        FilesPlume.deleteDir(workingDirectory.toFile());
        iteration++;
      }
    }
    return javaFileWriter.writeClassCode(packageName, classname, classSource);
  }

  @Override
  public Path writeUnmodifiedClassCode(String packageName, String classname, String javaCode)
      throws RandoopOutputException {
    return javaFileWriter.writeClassCode(packageName, classname, javaCode);
  }

  /**
   * Comments out lines with unnecessary catch or try statements. Fails if any other compilation
   * errors exist. Ignores compilation warnings.
   *
   * @param packageName the package name of the test class
   * @param javaCode the source code for the test class; each assertion must be on its own line
   * @param diagnostics the errors and warnings from compiling the class
   * @param destinationDir the directory that contains the source code, used only for debugging
   * @param e the exception that was raised when compiling the source code, used only for debugging
   * @return the class source edited so that failing assertions are replaced by comments
   * @throws RandoopBug if there is an unhandled compilation error (i.e., not about an unnecessary
   *     catch or try statement)
   */
  private String commentCatchStatements(
      String packageName,
      String javaCode,
      List<Diagnostic<? extends JavaFileObject>> diagnostics,
      Path destinationDir,
      FileCompiler.FileCompilerException e) {
    assert !Objects.equals(packageName, "");

    String[] javaCodeLines = javaCode.split(Globals.lineSep);

    for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics) {
      if (diagnostic.getKind() != Diagnostic.Kind.ERROR) {
        continue;
      }

      String msg = diagnostic.getMessage(null);
      int lineNumber = (int) diagnostic.getLineNumber();

      if (msg.contains("is never thrown in body of corresponding try statement")) {
        javaCodeLines[lineNumber - 1] = "// flaky: " + javaCodeLines[lineNumber - 1];
      } else if (msg.contains("'try' without 'catch', 'finally' or resource declarations")) {
        javaCodeLines[lineNumber - 1] = "{ // flaky: " + javaCodeLines[lineNumber - 1];
      } else {
        System.out.println("unhandled diagnostic: " + diagnostic.getMessage(null));
        compilationError( // sourceFile,
            destinationDir, javaCode, diagnostics, e);
      }
    }

    // TODO: For efficiency, have this method return the array and redo writeClass so that it writes
    // from array (?).
    return StringsPlume.joinLines(javaCodeLines);
  }

  /**
   * Issue an exception because of a non-recoverable compilation error.
   *
   * @param destinationDir the directory that contains the source code, used only for debugging
   * @param classSource the text of the test class
   * @param diagnostics the errors and warnings from compiling the class
   * @param e the exception that was raised when compiling the source code, used only for debugging
   */
  private void compilationError(
      // String sourceFile,
      Path destinationDir,
      String classSource,
      List<Diagnostic<? extends JavaFileObject>> diagnostics,
      FileCompiler.FileCompilerException e) {

    String message =
        String.format(
            "Compilation error during flaky-test filtering: fileCompiler.compile(%s, %s)%n",
            "sourceFile", destinationDir);
    if (GenInputsAbstract.print_non_compiling_file) {
      message += String.format("Source file:%n%s%n", classSource);
    } else {
      message +=
          String.format(
              "Use --print-non-compiling-file to print the file with the compilation error.%n");
    }
    message += String.format("Diagnostics:%n%s%n", diagnostics);
    throw new RandoopBug(message, e);
  }

  /**
   * Comments out lines with failing assertions. Uses the failures in the {@code status} from
   * running JUnit with {@code javaCode} to identify lines with failing assertions.
   *
   * @param packageName the package name of the test class
   * @param classname the simple (unqualified) name of the test class
   * @param javaCode the source code for the test class; each assertion must be on its own line
   * @param status the result of running the test with JUnit
   * @param flakyTests names of flaky tests, e.g. "test005". This is an output parameter that is
   *     augmented by this method.
   * @return the class source edited so that failing assertions are replaced by comments
   * @throws RandoopBug if {@code status} contains output for a failure not involving a
   *     Randoop-generated test method
   */
  private String commentFailingAssertions(
      String packageName,
      String classname,
      String javaCode,
      Status status,
      HashSet<String> flakyTests) {
    assert !Objects.equals(packageName, "");
    String qualifiedClassname = packageName == null ? classname : packageName + "." + classname;

    // Iterator to move through JUnit output. (JUnit only writes to standard output.)
    Iterator<String> lineIterator = status.standardOutputLines.iterator();

    // numJunitFailures returns a non-negative integer.
    int totalFailures = numJunitFailures(lineIterator, status, qualifiedClassname, javaCode);

    // Then, read the rest of the file to find each failure.

    // Split Java code text so that we can match the line number for the assertion with the code.
    // Use same line break as used to write test class file.
    String[] javaCodeLines = javaCode.split(Globals.lineSep);

    for (int failureCount = 0; failureCount < totalFailures; failureCount++) {
      // Read until beginning of failure
      Match failureHeaderMatch = readUntilMatch(lineIterator, FAILURE_HEADER_PATTERN);
      String line = failureHeaderMatch.line;
      String methodName = failureHeaderMatch.group;

      // Check that the method name in the failure message is a test method.
      if (!methodName.matches(GenTests.TEST_METHOD_NAME_PREFIX + "\\d+")) {
        System.out.println();
        System.out.printf("Failure in commentFailingAssertions(%s, %s)%n", packageName, classname);
        System.out.printf("javaCode =%n%s%n", javaCode);
        System.out.printf("status =%n%s%n", status);
        System.out.println();
        if (line.contains("initializationError")) {
          throw new RandoopBug(
              "Check configuration of test environment: "
                  + "initialization error of test in flaky-test filter: "
                  + line);
        } else {
          throw new RandoopBug("Bad method name " + methodName + " in flaky-test filter: " + line);
        }
      }

      flakyTests.add(methodName);

      // Search for the stacktrace entry corresponding to the test method, and capture the line
      // number.
      Pattern linePattern =
          Pattern.compile(
              String.format(
                  "\\s+at\\s+\\Q%s\\E\\.\\Q%s\\E\\(\\Q%s\\E\\.java:(\\d+)\\)",
                  qualifiedClassname, methodName, classname));

      Match failureLineMatch;
      try {
        failureLineMatch = readUntilMatch(lineIterator, linePattern);
      } catch (NotMatchedException e) {
        System.out.printf("failureCount = %d, totalFailures = %d%n", failureCount, totalFailures);
        System.out.println("Didn't find " + linePattern + "in:");
        for (String line2 : status.standardOutputLines) {
          System.out.print(line2);
        }
        System.out.println("End of output for didn't find " + linePattern);
        throw e;
      }

      // lineNumber is 1-based, not 0-based
      int lineNumber = Integer.parseInt(failureLineMatch.group);
      if (lineNumber < 1 || lineNumber > javaCodeLines.length) {
        throw new RandoopBug(
            String.format(
                "Line number %d read from JUnit is out of range [1,%d]: %s",
                lineNumber, javaCodeLines.length, failureLineMatch.line));
      }

      if (GenInputsAbstract.flaky_test_behavior == FlakyTestAction.HALT) {
        StringBuilder message = new StringBuilder();
        message.append(
            String.join(
                System.lineSeparator(),
                "A test code assertion failed during flaky-test filtering. Most likely,",
                "you ran Randoop on a program with nondeterministic behavior. See section",
                "\"Nondeterminism\" in the Randoop manual for ways to diagnose and handle this.",
                String.format(
                    "Class: %s, Method: %s, Line number: %d, Source line:%n%s%n",
                    classname, methodName, lineNumber, javaCodeLines[lineNumber - 1])));

        // fromLine and toLine are 0-based.
        int fromLine = lineNumber - 1;
        while (fromLine > 0 && !javaCodeLines[fromLine].contains("@Test")) {
          fromLine--;
        }
        int toLine = lineNumber;
        while (toLine < javaCodeLines.length && !javaCodeLines[toLine].contains("@Test")) {
          toLine++;
        }
        message.append(String.format("Containing method:%n"));
        for (int i = fromLine; i < toLine; i++) {
          message.append(String.format("%s%n", javaCodeLines[i]));
        }

        if (GenInputsAbstract.print_non_compiling_file) {
          message.append(String.format("Full source file:%n%s%n", javaCode));
        } else {
          message.append(
              String.format(
                  "Use --print-non-compiling-file to print the full file with the flaky test.%n"));
        }
        throw new RandoopUsageError(message.toString());
      }

      javaCodeLines[lineNumber - 1] = flakyLineReplacement(javaCodeLines[lineNumber - 1]);
    }

    // TODO: For efficiency, have this method return the array and redo writeClass so that it writes
    // from array (?).
    return StringsPlume.joinLines(javaCodeLines);
  }

  /**
   * Return the number of JUnit failures, parsed from the JUnit output.
   *
   * @param lineIterator an iterator over the lines of JUnit output
   * @param status the result of running JUnit
   * @param qualifiedClassname the name of the JUnit class, used only for debugging output
   * @param javaCode the JUnit class source code, used only for debugging output
   * @return the number of JUnit failures, a non-negative integer
   */
  private int numJunitFailures(
      Iterator<String> lineIterator, Status status, String qualifiedClassname, String javaCode) {
    Match failureCountMatch;
    try {
      failureCountMatch = readUntilMatch(lineIterator, FAILURE_MESSAGE_PATTERN);
    } catch (NotMatchedException e) {
      if (status.errorOutputLines.size() == 1) {
        String stderr = status.errorOutputLines.get(0);
        if (stderr.equals("Error: Could not find or load main class org.junit.runner.JUnitCore")) {
          throw new RandoopUsageError(
              "Classpath does not contain JUnit.  "
                  + "Please correct the classpath and re-run Randoop.");
        }
      }
      StringBuilder errorMessage = new StringBuilder();
      if (status.exitStatus == 137) {
        errorMessage.append("Exit status 137.  Probably interrupted or out of memory.");
        errorMessage.append(Globals.lineSep);
      } else {
        errorMessage.append(
            String.format(
                "Did not find \"%s\" in execution of %s%nstatus=%s%n",
                FAILURE_MESSAGE_PATTERN.pattern(), qualifiedClassname, status));
      }
      errorMessage.append("Standard output:");
      errorMessage.append(Globals.lineSep);
      for (String line : status.standardOutputLines) {
        errorMessage.append(line);
        errorMessage.append(Globals.lineSep);
      }
      errorMessage.append("... end of standard output.");
      errorMessage.append("Error output:");
      errorMessage.append(Globals.lineSep);
      for (String line : status.errorOutputLines) {
        errorMessage.append(line);
        errorMessage.append(Globals.lineSep);
      }
      errorMessage.append("... end of error output.");
      errorMessage.append(Globals.lineSep);
      if (AbstractGenerator.dump_sequences) {
        errorMessage.append(Globals.lineSep);
        errorMessage.append("Generated tests:");
        errorMessage.append(Globals.lineSep);
        errorMessage.append(javaCode);
      }
      if (status.exitStatus == 137) {
        throw new RandoopUsageError(errorMessage.toString(), e);
      } else {
        throw new RandoopBug(errorMessage.toString(), e);
      }
    }
    int totalFailures = Integer.parseInt(failureCountMatch.group);
    if (totalFailures <= 0) {
      throw new RandoopBug("JUnit has non-zero exit status, but no failure found");
    }
    return totalFailures;
  }

  /**
   * Given a flaky line (one that throws an exception but was not expected to), return a commented
   * version of the line that does no computation.
   *
   * @param flakyLine the line that throws an exception
   * @return the line, with its computation commented out
   */
  private String flakyLineReplacement(String flakyLine) {
    Matcher varDeclMatcher = VARIABLE_DECLARATION_LINE.matcher(flakyLine);
    if (varDeclMatcher.matches()) {
      String varType = varDeclMatcher.group(2);
      String newInitializer;
      switch (varType) {
        case "boolean":
          newInitializer = "false";
          break;
        case "byte":
        case "char":
        case "short":
        case "int":
          newInitializer = "0";
          break;
        case "long":
          newInitializer = "0L";
          break;
        case "float":
          newInitializer = "0.0f";
          break;
        case "double":
          newInitializer = "0.0";
          break;
        default:
          newInitializer = "null";
      }
      return varDeclMatcher.group(1) + newInitializer + "; // flaky: " + varDeclMatcher.group(3);
    } else {
      return "// flaky: " + flakyLine;
    }
  }

  /**
   * Reads lines of the JUnit output using the iterator until finding a match for the pattern, and
   * then returns a pair containing the line and the text matching the first group of the pattern.
   * Assumes that there is a match, and that the pattern has at least one group.
   *
   * @param lineIterator the iterator for reading from the JUnit output
   * @param pattern the pattern for a regex with at least one group
   * @return the pair containing the line and the text matching the first group
   * @throws RandoopBug if the iterator has no more lines, but the pattern hasn't been matched
   */
  private Match readUntilMatch(Iterator<String> lineIterator, Pattern pattern) {
    // Not a for loop because the iterator is side effected and passed around.
    while (lineIterator.hasNext()) {
      String line = lineIterator.next();
      Matcher matcher = pattern.matcher(line);
      if (matcher.matches()) {
        return new Match(line, matcher.group(1));
      }
    }
    throw new NotMatchedException(pattern);
  }

  /** An exception that indicates that an expected pattern was not found. */
  private static class NotMatchedException extends RuntimeException {
    private static final long serialVersionUID = 20171024;
    /** The pattern that was not found. */
    public final Pattern pattern;

    /**
     * Create a new NotMatchedException.
     *
     * @param pattern the pattern that was not found
     */
    public NotMatchedException(Pattern pattern) {
      this.pattern = pattern;
    }

    @Override
    public String getMessage() {
      return "NotMatchedException(" + pattern + ")";
    }
  }

  /**
   * Compiles the Java files in the list of files and writes the resulting class files to the
   * directory.
   *
   * @param packageName the package name for the test class
   * @param classname the name of the test class
   * @param classSource the text of the test class
   * @param destinationDir the directory for class file output
   * @return the name of the file
   * @throws FileCompiler.FileCompilerException if the file does not compile
   */
  private Path compileTestClass(
      String packageName, String classname, String classSource, Path destinationDir)
      throws FileCompiler.FileCompilerException {
    // TODO: The use of FileCompiler is temporary. Should be replaced by use of SequenceCompiler,
    // which will compile from source, once it is able to write the class file to disk.
    Path sourceFile;
    try {
      sourceFile = javaFileWriter.writeClassCode(packageName, classname, classSource);
    } catch (RandoopOutputException e) {
      throw new RandoopBug("Output error during flaky-test filtering", e);
    }
    FileCompiler fileCompiler = new FileCompiler();
    fileCompiler.compile(sourceFile, destinationDir);
    return sourceFile;
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
      return workingDirectory;
    } catch (IOException e) {
      // not RandoopBug
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
