package randoop.main.minimizer;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import randoop.main.Minimize;

public class MinimizerTests {
  private static final String pathSeparator = System.getProperty("path.separator");
  private static final String fileSeparator = System.getProperty("file.separator");

  /** Directory containing test inputs: suites to be minimized and goal minimized versions. */
  private static final String testDir = "test" + fileSeparator + "minimizer" + fileSeparator;

  /** The junit.jar file. */
  private static final String JUNIT_JAR = getJunitJar();

  private static String getJunitJar() {
    Path dir = Paths.get(System.getProperty("user.dir")).getParent().getParent();
    String command = "./gradlew -q printJunitJarPath";
    // This sometimes fails with timeout, sometimes with out of memory.  Why?
    // A 5-second timeout is not enough locally, a 10-second timeout is not enough on Travis (!).
    for (int i = 0; i < 3; i++) {
      Minimize.Outputs outputs = Minimize.runProcess(command, dir, 15);
      if (outputs.isSuccess()) {
        return outputs.stdout;
      }
      System.out.println(outputs.diagnostics());
    }
    System.out.println("Failed to run: " + command);
    System.out.println("Working directory: " + System.getProperty("user.dir"));
    System.exit(1);
    throw new Error("This can't happen");
  }

  /**
   * Test the minimizer with an input file. Uses no extra classpath dependencies and a timeout of 30
   * seconds.
   *
   * @param filename the name of the file containing the test suite, in directory {@link #testDir}
   * @throws IOException thrown if output or expected output files can't be read
   */
  private void testWithInput(String filename) throws IOException {
    testWithInput(testDir + filename, null, 30, true);
  }

  /**
   * Test the minimizer with an input file.
   *
   * @param inputFilePath path to a JUnit test suite
   * @param dependencies dependencies needed to compile and run the input file. This parameter is an
   *     array of Strings, each representing an element of the classpath, for instance, a directory
   *     or a jar file.
   * @param timeoutLimit maximum number of seconds allowed for any test case within the input file
   *     to run
   * @throws IOException thrown if output or expected output files can't be read
   */
  private void testWithInput(
      String inputFilePath, String[] dependencies, int timeoutLimit, boolean verboseOutput)
      throws IOException {
    String outputFilePath =
        new StringBuilder(inputFilePath)
            .insert(inputFilePath.lastIndexOf('.'), "Minimized")
            .toString();
    String expectedFilePath = inputFilePath + ".expected";

    // Obtain file object references.
    Path inputFile = Paths.get(inputFilePath);
    Path outputFile = Paths.get(outputFilePath);
    Path expectedFile = Paths.get(expectedFilePath);

    String classPath = JUNIT_JAR;
    if (dependencies != null) {
      for (String s : dependencies) {
        Path file = Paths.get(s);
        classPath += (pathSeparator + file.toAbsolutePath().toString());
      }
    }

    // Create the arguments array and invoke the minimizer.
    Minimize.mainMinimize(inputFile, classPath, timeoutLimit, verboseOutput);

    // Compare obtained and expected output.
    if (!FileUtils.contentEqualsIgnoreEOL(outputFile.toFile(), expectedFile.toFile(), null)) {
      System.out.println("expectedFile:");
      System.out.println(FileUtils.readFileToString(expectedFile.toFile(), (String) null));
      System.out.println("outputFile:");
      System.out.println(FileUtils.readFileToString(outputFile.toFile(), (String) null));
      assertTrue(false);
    }
  }

  @Test
  public void testWithComments() throws IOException {
    testWithInput("TestInputWithComments.java");
  }

  @Test
  public void testWithMulitpleTestCases() throws IOException {
    testWithInput("TestInputWithMulitpleTestCases.java");
  }

  @Test
  public void testWithWhileLoop() throws IOException {
    testWithInput("TestInputWithWhileLoop.java");
  }

  @Test
  public void testSimplifyRightHandSideValues() throws IOException {
    testWithInput("TestInputSimplifyRightHandSideValues.java");
  }

  @Test
  public void testWithImportsWithSameClassName() throws IOException {
    testWithInput("TestInputImportsWithSameClassName.java");
  }

  @Test
  public void testWithInputInSubDirectory() throws IOException {
    testWithInput(
        "testrootdir" + fileSeparator + "testsubdir" + fileSeparator + "TestInputSubDir1.java");
  }

  @Test
  public void testWithPassingAssertionValue() throws IOException {
    testWithInput("TestInputWithPassingAssertionValue.java");
  }

  @Test
  public void testWithMultiplePassingAssertions() throws IOException {
    testWithInput("TestInputWithMultiplePassingAssertions.java");
  }

  @Test
  public void testWithWildcardImport() throws IOException {
    testWithInput("TestInputWithWildcardImport.java");
  }

  @Test
  public void testWithMultipleVarDeclarationsOnALine() throws IOException {
    testWithInput("TestInputMultipleVarDeclarationsOnALine.java");
  }

  @Test
  public void testWithNoFailingTests() throws IOException {
    testWithInput("TestInputWithNoFailingTests.java");
  }

  @Test
  public void testWithVariableReassignment() throws IOException {
    testWithInput("TestInputWithVariableReassignment.java");
  }

  @Test
  public void testWithRuntimeException() throws IOException {
    testWithInput("TestInputWithRuntimeException.java");
  }

  @Test
  public void testWithWrappedTypes() throws IOException {
    testWithInput("TestInputWithWrappedTypes.java");
  }

  @Test
  public void testWithNonCompilingTest() throws IOException {
    // Path to input file.
    String inputFilePath = testDir + "TestInputWithNonCompilingTest.java";
    String timeout = "30";

    // Obtain file object references.
    Path inputFile = Paths.get(inputFilePath);

    // Classpath obtained by adding the necessary components together.
    String classPath = null;

    // Create the arguments array and invoke the minimizer.
    assertFalse(Minimize.mainMinimize(inputFile, classPath, Integer.parseInt(timeout), false));
  }
}
