package randoop.main.minimizer;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.StringJoiner;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import randoop.main.Minimize;

public class MinimizerTests {

  /** Directory containing test inputs: suites to be minimized and goal minimized versions. */
  private static final String testDir = "test" + File.separator + "minimizer" + File.separator;

  /** The classpath of JUnit: junit.jar and the jar files that it depends on. */
  private static final String JUNIT_JAR = getJunitJar();

  /**
   * Returns the classpath of JUnit, which the build passes to the tests as a system property.
   *
   * @return the classpath of JUnit
   */
  private static String getJunitJar() {
    String result = System.getProperty("junit.classpath");
    if (result == null) {
      throw new Error("Set system property junit.classpath to the classpath of JUnit");
    }
    return result;
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

    StringJoiner classPath = new StringJoiner(File.pathSeparator);
    classPath.add(JUNIT_JAR);
    if (dependencies != null) {
      for (String s : dependencies) {
        classPath.add(Paths.get(s).toAbsolutePath().toString());
      }
    }

    // Create the arguments array and invoke the minimizer.
    Minimize.mainMinimize(inputFile, classPath.toString(), timeoutLimit, verboseOutput);

    // Compare obtained and expected output.
    if (!FileUtils.contentEqualsIgnoreEOL(outputFile.toFile(), expectedFile.toFile(), null)) {
      System.out.println("---------------- expectedFile: ----------------");
      System.out.println(FileUtils.readFileToString(expectedFile.toFile(), (String) null));
      System.out.println("---------------- outputFile: ----------------");
      System.out.println(FileUtils.readFileToString(outputFile.toFile(), (String) null));
      System.out.println("---------------- End of files. ----------------");
      throw new Error("Files differ (see output above): " + expectedFile + " " + outputFile);
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
        "testrootdir" + File.separator + "testsubdir" + File.separator + "TestInputSubDir1.java");
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
    System.out.printf("\"Error when compiling\" output EXPECTED below.%n%n");

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
