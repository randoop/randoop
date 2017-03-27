package randoop.main.minimizer;

import static org.junit.Assert.*;

import org.junit.Test;
import randoop.main.Minimize;

public class MinimizerComponentsTests {

  /**
   * Tests the {@link Minimize#getClassName} method, which obtains a class name from the complete
   * path to a class file.
   */
  @Test
  public void getClassNameTest() {
    String fileSeparator = System.getProperty("file.separator");
    String filePath = "";

    if (fileSeparator.equals("/")) {
      filePath = "C:/Users/Waylon/workspace/RandoopTestMinimizer/src/test/MinimizerTest.java";
    } else {
      filePath =
          "C:\\Users\\Waylon\\workspace\\RandoopTestMinimizer\\src\\test\\MinimizerTest.java";
    }

    String className = "MinimizerTest";
    assertEquals(className, Minimize.getClassName(filePath));
  }
}
