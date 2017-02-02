package randoop.main.minimizer;

import static org.junit.Assert.*;
import org.junit.Test;
import randoop.main.Minimize;

import java.util.HashSet;
import java.util.Set;

public class MinimizerComponentsTests {

  /**
   * Tests the getClassName method and checks that the minimizer properly
   * obtains a class name from a complete class path
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

    String nullFilePath = null;
    assertNull(Minimize.getClassName(nullFilePath));
  }

  /**
   * Tests the getSimpleTypeName method and checks that the minimizer properly
   * obtains a simple type name from a fully qualified type name
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

  /**
   * Tests the getFullyQualifiedTypeNames method and checks that the minimizer
   * properly obtains all the proper fully qualified name components from a
   * given fully qualified name
   */
  @Test
  public void getFullyQualifiedTypeNames() {
    String fullyQualifiedName;
    Set<String> fullyQualifiedNames = new HashSet<String>();
    Set<String> result = new HashSet<String>();

    fullyQualifiedName =
        "org.apache.commons.lang3.tuple.MutablePair<org.apache.commons.lang3.concurrent.CircuitBreakingException, org.apache.commons.lang3.text.translate.NumericEntityUnescaper.OPTION>";
    fullyQualifiedNames.add("org.apache.commons.lang3.tuple.MutablePair");
    fullyQualifiedNames.add("org.apache.commons.lang3.concurrent.CircuitBreakingException");
    fullyQualifiedNames.add(
        "org.apache.commons.lang3.text.translate.NumericEntityUnescaper.OPTION");

    result = Minimize.getFullyQualifiedTypeNames(fullyQualifiedName);

    for (String typeName : fullyQualifiedNames) {
      assertTrue(result.contains(typeName));
    }

    fullyQualifiedNames.clear();

    fullyQualifiedName = "java.util.HashMap<java.lang.String, java.lang.Double>";
    fullyQualifiedNames.add("java.util.HashMap");
    fullyQualifiedNames.add("java.lang.String");
    fullyQualifiedNames.add("java.lang.Double");

    result = Minimize.getFullyQualifiedTypeNames(fullyQualifiedName);

    for (String typeName : fullyQualifiedNames) {
      assertTrue(result.contains(typeName));
    }
  }
}
