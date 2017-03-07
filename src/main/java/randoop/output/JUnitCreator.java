package randoop.output;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import randoop.sequence.ExecutableSequence;

import static randoop.output.NameGenerator.numDigits;

/**
 * Creates Java source as {@code String} for a suite of JUnit4 tests.
 *
 */
public class JUnitCreator {

  private final String packageName;
  private final String testMethodPrefix;
  private final Set<String> testClassNames;

  /**
   * classMethodCounts maps test class names to the number of methods in each
   * class. This is used to generate lists of method names for a class, since
   * current convention is that a test method is named "test"+i for some integer
   * i.
   */
  private Map<String, Integer> classMethodCounts;

  /** The Java text for BeforeAll method of generated test class. */
  private List<String> beforeAllText = null;

  /** The Java text for AfterAll method of generated test class. */
  private List<String> afterAllText = null;

  /** The Java text for BeforeEach method of generated test class. */
  private List<String> beforeEachText = null;

  /** The Java text for AfterEach method of generated test class. */
  private List<String> afterEachText = null;

  /** The JUnit annotation for the BeforeAll option */
  private static final String BEFORE_ALL = "@BeforeClass";

  /** The JUnit annotation for the AfterAll option */
  private static final String AFTER_ALL = "@AfterClass";

  /** The JUnit annotation for the BeforeEach option */
  private static final String BEFORE_EACH = "@Before";

  /** The JUnit annotation for the AfterEach option */
  private static final String AFTER_EACH = "@After";

  /** The method name for the BeforeAll option */
  private static final String BEFORE_ALL_METHOD = "setupAll";

  /** The method name for the AfterAll option */
  private static final String AFTER_ALL_METHOD = "teardownAll";

  /** The method name for the BeforeEach option */
  private static final String BEFORE_EACH_METHOD = "setup";

  /** The method name for the AfterEach option */
  private static final String AFTER_EACH_METHOD = "teardown";

  public static JUnitCreator getTestCreator(
      String junit_package_name,
      String testMethodPrefix,
      List<String> beforeAllText,
      List<String> afterAllText,
      List<String> beforeEachText,
      List<String> afterEachText) {
    JUnitCreator junitCreator = new JUnitCreator(junit_package_name, testMethodPrefix);
    if (beforeAllText != null) {
      junitCreator.addBeforeAll(beforeAllText);
    }
    if (afterAllText != null) {
      junitCreator.addAfterAll(afterAllText);
    }
    if (beforeEachText != null) {
      junitCreator.addBeforeEach(beforeEachText);
    }
    if (afterEachText != null) {
      junitCreator.addAfterEach(afterEachText);
    }
    return junitCreator;
  }

  private JUnitCreator(String packageName, String testMethodPrefix) {
    this.packageName = packageName;
    this.testMethodPrefix = testMethodPrefix;
    this.testClassNames = new TreeSet<>();
    this.classMethodCounts = new LinkedHashMap<>();
  }

  /**
   * Add text for BeforeClass-annotated method in each generated test class.
   * @param text  the (Java) text for method
   */
  private void addBeforeAll(List<String> text) {
    this.beforeAllText = text;
  }

  /**
   * Add text for AfterClass-annotated method in each generated text class.
   * @param text  the (Java) text for method
   */
  private void addAfterAll(List<String> text) {
    this.afterAllText = text;
  }

  /**
   * Add text for Before-annotated method in each generated test class.
   * @param text  the (Java) text for method
   */
  private void addBeforeEach(List<String> text) {
    this.beforeEachText = text;
  }

  /**
   * Add text for After-annotated method in each generated test class.
   * @param text  the (Java) text for method
   */
  private void addAfterEach(List<String> text) {
    this.afterEachText = text;
  }

  public String createTestClass(String testClassName, List<ExecutableSequence> sequences) {
    this.testClassNames.add(testClassName);
    this.classMethodCounts.put(testClassName, sequences.size());
    ClassSourceBuilder sourceBuilder = new ClassSourceBuilder(testClassName, packageName);
    List<String> imports = new ArrayList<>();
    if (afterEachText != null) {
      imports.add("org.junit.After;");
    }
    if (afterAllText != null) {
      imports.add("org.junit.AfterClass;");
    }
    if (beforeEachText != null) {
      imports.add("org.junit.Before;");
    }
    if (beforeAllText != null) {
      imports.add("org.junit.BeforeClass;");
    }
    imports.add("org.junit.FixMethodOrder;");
    imports.add("org.junit.Test;");
    imports.add("org.junit.runners.MethodSorters;");
    sourceBuilder.addImports(imports);

    List<String> annotations = new ArrayList<>();
    annotations.add("@FixMethodOrder(MethodSorters.NAME_ASCENDING)");
    sourceBuilder.addAnnotation(annotations);

    sourceBuilder.addMember("public static boolean debug = false;");

    if (beforeAllText != null) {
      sourceBuilder.addMember(
          createFixture(BEFORE_ALL, "public static", BEFORE_ALL_METHOD, beforeAllText));
    }
    if (afterAllText != null) {
      sourceBuilder.addMember(
          createFixture(AFTER_ALL, "public static", AFTER_ALL_METHOD, afterAllText));
    }
    if (beforeEachText != null) {
      sourceBuilder.addMember(
          createFixture(BEFORE_EACH, "public", BEFORE_EACH_METHOD, beforeEachText));
    }
    if (afterEachText != null) {
      sourceBuilder.addMember(
          createFixture(AFTER_EACH, "public", AFTER_EACH_METHOD, afterEachText));
    }

    NameGenerator methodNameGen =
        new NameGenerator(testMethodPrefix, 1, numDigits(sequences.size()));
    for (ExecutableSequence s : sequences) {
      sourceBuilder.addMember(createTestMethod(testClassName, methodNameGen.next(), s));
    }

    return sourceBuilder.toString();
  }

  /**
   * Creates a test method as a {@code String} for the sequence {@code testSequence}.
   *
   * @param className  the name of the test class
   * @param methodName  the name of the test method
   * @param testSequence  the {@link ExecutableSequence} test sequence
   * @return the {@code String} for the test method
   */
  private List<String> createTestMethod(
      String className, String methodName, ExecutableSequence testSequence) {
    List<String> throwsList = new ArrayList<>();
    throwsList.add("Throwable");
    MethodSourceBuilder sourceBuilder =
        new MethodSourceBuilder("public", "void", methodName, new ArrayList<String>(), throwsList);
    List<String> annotation = new ArrayList<>();
    annotation.add("@Test");
    sourceBuilder.addAnnotation(annotation);
    String debugString =
        "if (debug) { System.out.format(\"%n%s%n\",\"" + className + "." + methodName + "\"); }";
    sourceBuilder.addBodyText(debugString);
    sourceBuilder.addBodyText(testSequence.toCodeLines());
    return sourceBuilder.toLines();
  }

  /**
   * Creates the declaration of a single test fixture.
   *
   * @param annotation  the fixture annotation
   * @param modifier  the method modifiers for fixture declaration
   * @param methodName  the name of the fixture method
   * @param bodyText  the text of the fixture method
   * @return the fixture method as a {@code String}
   */
  private List<String> createFixture(
      String annotation, String modifier, String methodName, List<String> bodyText) {
    MethodSourceBuilder methodSourceBuilder =
        new MethodSourceBuilder(
            modifier, "void", methodName, new ArrayList<String>(), new ArrayList<String>());
    List<String> annotationList = new ArrayList<>();
    annotationList.add(annotation);
    methodSourceBuilder.addAnnotation(annotationList);
    methodSourceBuilder.addBodyText(bodyText);
    return methodSourceBuilder.toLines();
  }

  /**
   * Creates the JUnit4 suite class for the tests in this object as a {@code String}.
   *
   * @param suiteClassName  the name of the suite class created
   * @return the {@code String} with the declaration for the suite class
   */
  public String createSuiteClass(String suiteClassName) {
    ClassSourceBuilder sourceBuilder = new ClassSourceBuilder(suiteClassName, packageName);

    List<String> imports = new ArrayList<>();
    imports.add("org.junit.runner.RunWith;");
    imports.add("org.junit.runners.Suite;");
    sourceBuilder.addImports(imports);

    List<String> annotations = new ArrayList<>();
    annotations.add("@RunWith(Suite.class)");
    annotations.add("@Suite.SuiteClasses({");

    Iterator<String> testIterator = testClassNames.iterator();
    if (testIterator.hasNext()) {
      String classString = testIterator.next() + ".class";
      while (testIterator.hasNext()) {
        annotations.add(classString + ", ");
        classString = testIterator.next() + ".class";
      }
      annotations.add(classString);
    }
    annotations.add("})");
    sourceBuilder.addAnnotation(annotations);
    return sourceBuilder.toString();
  }

  /**
   * Create non-reflective test driver as a main class.
   *
   * @param driverName  the name for the driver class
   * @return the test driver class as a {@code String}
   */
  public String createTestDriver(String driverName) {
    ClassSourceBuilder sourceBuilder = new ClassSourceBuilder(driverName, packageName);
    List<String> arguments = new ArrayList<>();
    arguments.add("String[] args");
    MethodSourceBuilder methodSourceBuilder =
        new MethodSourceBuilder(
            "public static", "void", "main", arguments, new ArrayList<String>());
    methodSourceBuilder.addBodyText("boolean wasSuccessful = true;");

    NameGenerator instanceNameGen = new NameGenerator("t");
    for (String testClass : testClassNames) {
      if (beforeAllText != null) {
        methodSourceBuilder.addBodyText(testClass + "." + BEFORE_ALL_METHOD + "();");
      }

      String testVariable = instanceNameGen.next();
      methodSourceBuilder.addBodyText(
          testClass + " " + testVariable + "= new " + testClass + "();");

      int classMethodCount = classMethodCounts.get(testClass);
      NameGenerator methodGen = new NameGenerator("test", 1, numDigits(classMethodCount));

      while (methodGen.nameCount() < classMethodCount) {
        if (beforeEachText != null) {
          methodSourceBuilder.addBodyText(testVariable + "." + BEFORE_EACH_METHOD + "();");
        }
        String methodName = methodGen.next();
        methodSourceBuilder.addBodyText("try {");
        methodSourceBuilder.addBodyText(testVariable + "." + methodName + "();");
        methodSourceBuilder.addBodyText("} catch (Throwable e) {");
        methodSourceBuilder.addBodyText("wasSuccessful = false;");
        methodSourceBuilder.addBodyText("e.printStackTrace();");
        methodSourceBuilder.addBodyText("}");
        if (afterEachText != null) {
          methodSourceBuilder.addBodyText(testVariable + "." + AFTER_EACH_METHOD + "();");
        }
      }

      if (afterAllText != null) {
        methodSourceBuilder.addBodyText(testClass + "." + AFTER_ALL_METHOD + "();");
      }
    }
    methodSourceBuilder.addBodyText("if ( !wasSuccessful ) {");
    methodSourceBuilder.addBodyText("System.exit(1);");
    methodSourceBuilder.addBodyText("}");
    String methodBody = methodSourceBuilder.toString();
    sourceBuilder.addMember(methodBody);
    return sourceBuilder.toString();
  }
}
