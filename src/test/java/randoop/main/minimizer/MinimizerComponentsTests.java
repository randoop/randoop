package randoop.main.minimizer;

import static org.junit.Assert.*;

import org.junit.Test;
import randoop.main.Minimize;

public class MinimizerComponentsTests {

  /**
   * Tests the getClassName method and checks that the minimizer properly obtains a class name from
   * a complete class path
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

  /**
   * Tests the getSimpleTypeName method and checks that the minimizer properly obtains a simple type
   * name from a fully qualified type name
   */
  @Test
  public void getSimpleNameTest() {
    String fullyQualifiedName;
    String simpleName;

    fullyQualifiedName = "org.apache.commons.lang3.concurrent.CircuitBreakingException";
    simpleName = "CircuitBreakingException";
    assertEquals(simpleName, Minimize.getSimpleTypeName(fullyQualifiedName));

    fullyQualifiedName = "org.apache.commons.lang3.tuple.MutablePair";
    simpleName = "MutablePair";
    assertEquals(simpleName, Minimize.getSimpleTypeName(fullyQualifiedName));

    fullyQualifiedName = "java.lang.String";
    simpleName = "String";
    assertEquals(simpleName, Minimize.getSimpleTypeName(fullyQualifiedName));

    fullyQualifiedName = "SimpleType";
    simpleName = "SimpleType";
    assertEquals(simpleName, Minimize.getSimpleTypeName(fullyQualifiedName));

    fullyQualifiedName = "java.util.HashMap";
    simpleName = "HashMap";
    assertEquals(simpleName, Minimize.getSimpleTypeName(fullyQualifiedName));
  }
}
