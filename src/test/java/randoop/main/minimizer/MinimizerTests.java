package randoop.main.minimizer;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.Test;
import randoop.main.Minimize;

public class MinimizerTests {
  // Obtain system separators for path and file
  private static final String pathSeparator = System.getProperty("path.separator");
  private static final String fileSeparator = System.getProperty("file.separator");

  // Directory to test inputs
  private static final String testDir = "test" + fileSeparator + "minimizer" + fileSeparator;

  /**
   * Test the minimizer with an input file
   *
   * @param inputFilePath input file to minimize: a JUnit test suite
   * @param dependencies dependencies needed to compile and run the input file. This parameter is an
   *     array of Strings, each representing an element of the classpath, for instance, a directory
   *     or a jar file.
   * @param timeoutLimit maximum number of seconds allowed for any test case within the input file
   *     to run
   * @throws IOException thrown if output or expected output files can't be read
   */
  private void testWithInput(String inputFilePath, String[] dependencies, String timeoutLimit)
      throws IOException {
    String outputFilePath =
        new StringBuilder(inputFilePath)
            .insert(inputFilePath.lastIndexOf('.'), "Minimized")
            .toString();
    String expectedFilePath = inputFilePath + ".expected";

    // Obtain File object references
    File inputFile = new File(inputFilePath);
    File outputFile = new File(outputFilePath);

    // Obtain the complete path to the input, output, and expected files
    inputFilePath = inputFile.getAbsolutePath();
    outputFilePath = outputFile.getAbsolutePath();

    // Classpath obtained by adding the necessary components together
    String classPath = null;
    if (dependencies != null) {
      classPath = "";
      for (String s : dependencies) {
        File file = new File(s);
        classPath += (pathSeparator + file.getAbsolutePath());
      }
    }

    // Create the arguments array and invoke the minimizer
    Minimize.mainMinimize(inputFilePath, classPath, Integer.parseInt(timeoutLimit));

    // Compare obtained and expected output
    String obtainedOutput = readFile(outputFilePath, Charset.defaultCharset());
    String expectedOutput = readFile(expectedFilePath, Charset.defaultCharset());

    // Handle carriage returns on different OS
    expectedOutput = expectedOutput.replaceAll("\\r\\n", "\n");
    obtainedOutput = obtainedOutput.replaceAll("\\r\\n", "\n");

    assertEquals(expectedOutput, obtainedOutput);
  }

  /**
   * Read a file and return the String representation
   *
   * @param filePath path to file
   * @param encoding character encoding
   * @return String representation of the input file
   * @throws IOException if the file can't be read
   */
  private static String readFile(String filePath, Charset encoding) throws IOException {
    byte[] encoded = Files.readAllBytes(Paths.get(filePath));
    return new String(encoded, encoding);
  }

  @Test
  public void test1() throws IOException {
    // Path to input file
    String inputFilePath = testDir + "TestInput1.java";
    String timeout = "30";

    testWithInput(inputFilePath, null, timeout);
  }

  @Test
  public void test2() throws IOException {
    // Path to input file
    String inputFilePath = testDir + "TestInput2.java";
    String timeout = "30";

    // Test input contains while loop.
    testWithInput(inputFilePath, null, timeout);
  }

  @Test
  public void test3() throws IOException {
    // Path to input file
    String inputFilePath = testDir + "TestInput3.java";
    String timeout = "30";

    testWithInput(inputFilePath, null, timeout);
  }

  @Test
  public void testWithImportsWithSameClassName() throws IOException {
    // Path to input file
    String inputFilePath = testDir + "TestInputImportsWithSameClassName.java";
    String timeout = "30";

    // This test input uses ClassA belonging to package dir_a and ClassA belonging to
    // package dir_b. This test checks that the minimizer doesn't remove the fully
    // qualified type name for both instances of ClassA.
    testWithInput(inputFilePath, null, timeout);
  }

  @Test
  public void testWithInputInSubDirectory() throws IOException {
    // Path to input file
    String inputFilePath =
        testDir
            + "testrootdir"
            + fileSeparator
            + "testsubdir"
            + fileSeparator
            + "TestInputSubDir1.java";
    String timeout = "30";

    // This test input file is located in a subdirectory and non-default package.
    // Checks that the minimizer is able to find the input file and minimize it.
    testWithInput(inputFilePath, null, timeout);
  }

  @Test
  public void testWithPassingAssertionValue() throws IOException {
    // Path to input file
    String inputFilePath = testDir + "TestInputWithPassingAssertionValue.java";
    String timeout = "30";

    testWithInput(inputFilePath, null, timeout);
  }

  @Test
  public void testWithWildcardImport() throws IOException {
    // Path to input file
    String inputFilePath = testDir + "TestInputWithWildcardImport.java";
    String timeout = "30";

    testWithInput(inputFilePath, null, timeout);
  }

  @Test
  public void testWithMultipleVarDeclarationsOnALine() throws IOException {
    // Path to input file
    String inputFilePath = testDir + "TestInputMultipleVarDeclarationsOnALine.java";
    String timeout = "30";

    testWithInput(inputFilePath, null, timeout);
  }

  @Test
  public void testWithNoFailingTests() throws IOException {
    // Path to input file
    String inputFilePath = testDir + "TestInputWithNoFailingTests.java";
    String timeout = "30";

    testWithInput(inputFilePath, null, timeout);
  }

  @Test
  public void testWithVariableReassignment() throws IOException {
    // Path to input file
    String inputFilePath = testDir + "TestInputWithVariableReassignment.java";
    String timeout = "30";

    testWithInput(inputFilePath, null, timeout);
  }

  @Test
  public void testWithNonCompilingTest() throws IOException {
    // Path to input file
    String inputFilePath = testDir + "TestInputWithNonCompilingTest.java";
    String timeout = "30";

    String outputFilePath =
        new StringBuilder(inputFilePath)
            .insert(inputFilePath.lastIndexOf('.'), "Minimized")
            .toString();

    // Obtain File object references
    File inputFile = new File(inputFilePath);

    // Obtain the complete path to the input, output, and expected files
    inputFilePath = inputFile.getAbsolutePath();

    // Classpath obtained by adding the necessary components together
    String classPath = null;

    // Create the arguments array and invoke the minimizer
    assertFalse(Minimize.mainMinimize(inputFilePath, classPath, Integer.parseInt(timeout)));
  }
}
