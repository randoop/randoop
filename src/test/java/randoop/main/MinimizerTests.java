package randoop.main;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;

public class MinimizerTests {
  // Obtain system separators for path and file
  private static final String pathSeparator = System.getProperty("path.separator");
  private static final String fileSeparator = System.getProperty("file.separator");

  private static final String testDir = "src" + fileSeparator + "test" + fileSeparator;
  private static final String dependencyDir = testDir + "dependencies" + fileSeparator;

  /**
   * Test the minimizer with an input file
   *
   * @param inputFilePath
   *            input file to minimize
   * @param dependecies
   *            dependencies needed to compile and run the input file
   * @param timeoutLimit
   *            maximum number of seconds allowed for any test case within the
   *            input file to run
   * @throws IOException
   *             thrown if output or expected output files can't be read
   */
  private void testWithInput(String inputFilePath, String[] dependecies, String timeoutLimit)
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
    String classPath = "";
    for (String s : dependecies) {
      File file = new File(s);
      classPath += (pathSeparator + file.getAbsolutePath());
    }

    // Create the arguments array and invoke the minimizer
    String[] args = {inputFilePath, classPath, timeoutLimit};
    // Minimize.handle(args);

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
   * @param filePath
   *            path to file
   * @param encoding
   *            character encoding
   * @return String representation of the input file
   * @throws IOException
   *             if the file can't be read
   */
  private static String readFile(String filePath, Charset encoding) throws IOException {
    byte[] encoded = Files.readAllBytes(Paths.get(filePath));
    return new String(encoded, encoding);
  }

  @Test
  public void test1() throws IOException {
    // Paths to input file
    String inputFilePath = testDir + "TestInput1.java";

    String commonsLangPath = dependencyDir + "commons-lang3-3.5.jar";
    String jUnitPath = dependencyDir + "junit-4.12.jar";
    String hamcrestPath = dependencyDir + "hamcrest-core-1.3.jar";

    String timeout = "30";

    testWithInput(inputFilePath, new String[] {commonsLangPath, jUnitPath, hamcrestPath}, timeout);
  }

  @Test
  public void test2() throws IOException {
    // Paths to input file
    String inputFilePath = testDir + "TestInput2.java";

    String commonsLangPath = dependencyDir + "commons-lang3-3.5.jar";
    String jUnitPath = dependencyDir + "junit-4.12.jar";
    String hamcrestPath = dependencyDir + "hamcrest-core-1.3.jar";

    String timeout = "30";

    testWithInput(inputFilePath, new String[] {commonsLangPath, jUnitPath, hamcrestPath}, timeout);
  }

  @Test
  public void test3() throws IOException {
    // Paths to input file
    String inputFilePath = testDir + "TestInput3.java";

    String jUnitPath = dependencyDir + "junit-4.12.jar";
    String hamcrestPath = dependencyDir + "hamcrest-core-1.3.jar";

    String timeout = "10";

    testWithInput(inputFilePath, new String[] {jUnitPath, hamcrestPath}, timeout);
  }

  @Test
  public void test4() throws IOException {
    // Paths to input file
    String inputFilePath = testDir + "TestInput4.java";

    String jUnitPath = dependencyDir + "junit-4.12.jar";
    String hamcrestPath = dependencyDir + "hamcrest-core-1.3.jar";

    String timeout = "10";

    testWithInput(inputFilePath, new String[] {jUnitPath, hamcrestPath}, timeout);
  }

  @Test
  public void test5() throws IOException {
    // Paths to input file
    String inputFilePath = testDir + "TestInput5.java";

    String jUnitPath = dependencyDir + "junit-4.12.jar";
    String hamcrestPath = dependencyDir + "hamcrest-core-1.3.jar";

    String timeout = "10";

    testWithInput(inputFilePath, new String[] {jUnitPath, hamcrestPath}, timeout);
  }
}
