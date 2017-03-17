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
    // Paths to input file
    String inputFilePath = testDir + "TestInput1.java";
    String timeout = "30";

    testWithInput(inputFilePath, /*dependences=*/ null, timeout);
  }

  @Test
  public void test2() throws IOException {
    // Paths to input file
    String inputFilePath = testDir + "TestInput2.java";
    String timeout = "30";

    testWithInput(inputFilePath, null, timeout);
  }

  @Test
  public void test3() throws IOException {
    // Paths to input file
    String inputFilePath = testDir + "TestInput3.java";
    String timeout = "10";

    testWithInput(inputFilePath, null, timeout);
  }
}
