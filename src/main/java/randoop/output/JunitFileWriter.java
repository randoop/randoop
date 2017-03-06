package randoop.output;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import randoop.sequence.ExecutableSequence;
import randoop.util.Log;

/**
 * JunitFileWriter is a class that for a collection of sequences, outputs Java
 * files containing one JUnit4 test method per sequence. An object manages the
 * information for a suite of tests (name, package, and directory) and is used
 * by first running writeJUnitTestFiles and then writeSuiteFile or
 * writeDriverFile.
 */
public class JunitFileWriter {

  // The class of the main JUnit suite, and the prefix of the subsuite names.
  private final String masterTestClassName;

  // The package name of the main JUnit suite
  private final String packageName;

  // The directory where the JUnit files should be written to.
  private final String dirName;

  /**
   * testClassCount indicates the number of test classes written for the code
   * partitions received by writeJUnitTestFiles. It is used to generate the list
   * of test class names.
   */
  private int testClassCount = 0;

  /**
   * classMethodCounts maps test class names to the number of methods in each
   * class. This is used to generate lists of method names for a class, since
   * current convention is that a test method is named "test"+i for some integer
   * i.
   */
  private Map<String, Integer> classMethodCounts = new LinkedHashMap<>();

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

  /**
   * JunitFileWriter creates an instance of class holding information needed to
   * write a test suite.
   *
   * @param junitDirName
   *          directory where files are to be written
   * @param packageName
   *          package name to be used in JUnit test classes
   * @param masterTestClassName
   *          name of test class suite/driver
   */
  public JunitFileWriter(String junitDirName, String packageName, String masterTestClassName) {
    this.dirName = junitDirName;
    this.packageName = packageName;
    this.masterTestClassName = masterTestClassName;
  }

  /**
   * Add text for BeforeClass-annotated method in each generated test class.
   * @param text  the (Java) text for method
   */
  public void addBeforeAll(List<String> text) {
    this.beforeAllText = text;
  }

  /**
   * Add text for AfterClass-annotated method in each generated text class.
   * @param text  the (Java) text for method
   */
  public void addAfterAll(List<String> text) {
    this.afterAllText = text;
  }

  /**
   * Add text for Before-annotated method in each generated test class.
   * @param text  the (Java) text for method
   */
  public void addBeforeEach(List<String> text) {
    this.beforeEachText = text;
  }

  /**
   * Add text for After-annotated method in each generated test class.
   * @param text  the (Java) text for method
   */
  public void addAfterEach(List<String> text) {
    this.afterEachText = text;
  }

  /**
   * writeJUnitTestFiles writes a suite of test class files from a list of lists
   * of executable sequences. Each executable sequence corresponds to a test
   * method, a list of executable sequences corresponds to a test class, and the
   * list of lists to a test suite.
   *
   * @param seqPartition
   *          suite of test classes as a list of lists of executable sequences
   * @return list of File objects corresponding to test class files generated
   *
   * @see #writeSuiteFile
   * @see #writeDriverFile
   */
  public List<File> writeJUnitTestFiles(List<List<ExecutableSequence>> seqPartition) {
    List<File> ret = new ArrayList<>();

    NameGenerator classNameGen = new NameGenerator(masterTestClassName);

    createOutputDir();

    for (List<ExecutableSequence> partition : seqPartition) {
      ret.add(writeTestClass(partition, classNameGen.next()));
    }

    testClassCount = classNameGen.nameCount();

    return ret;
  }

  /**
   * writeTestClass writes a code sequence as a JUnit4 test class to a .java
   * file. Tests are executed in ascending alphabetical order by test method
   * name.
   *
   * @param sequences
   *          list of executable sequences for method bodies
   * @param testClassName
   *          name of test class
   * @return the File object for generated java file
   */
  private File writeTestClass(List<ExecutableSequence> sequences, String testClassName) {

    File file = new File(getDir(), testClassName + ".java");
    PrintStream out = createTextOutputStream(file);

    NameGenerator methodNameGen = new NameGenerator("test", 1, numDigits(sequences.size()));

    String testClassString = createTestClassString(sequences, testClassName, methodNameGen);

    try {
      out.println(testClassString);
      classMethodCounts.put(testClassName, methodNameGen.nameCount());
    } finally {
      if (out != null) out.close();
    }

    return file;
  }

  private String createTestClassString(
      List<ExecutableSequence> sequences, String testClassName, NameGenerator methodNameGen) {
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

    for (ExecutableSequence s : sequences) {
      sourceBuilder.addMember(createTestMethod(testClassName, methodNameGen.next(), s));
    }

    return sourceBuilder.toString();
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
   * Generates the list of test class names for previously generated test
   * suites.
   *
   * @return list of class names
   */
  private List<String> getTestClassNames() {
    List<String> junitTestSuites = new LinkedList<>();
    NameGenerator classNameGen = new NameGenerator(masterTestClassName);
    while (classNameGen.nameCount() < testClassCount) {
      junitTestSuites.add(classNameGen.next());
    }
    return junitTestSuites;
  }

  /**
   * Writes a JUnit4 suite consisting of test classes from
   * {@link #writeJUnitTestFiles(List)} and additional classes provided as a
   * parameter. The file is written to the directory pointed to by writer object
   * in a class whose name is the {@link #masterTestClassName}.
   *
   * @return {@link File} object for test suite file.
   */
  public File writeSuiteFile() {
    String suiteClassName = masterTestClassName;
    String suiteFileString = getSuiteClassString(suiteClassName);

    File dir = this.getDir();
    File file = new File(dir, suiteClassName + ".java");
    try (PrintStream out = createTextOutputStream(file)) {
      out.println(suiteFileString);
    }
    return file;
  }

  /**
   * Creates the suite class for the tests in this object as a {@code String}.
   *
   * @param suiteClassName  the name of the suite class created
   * @return the {@code String} with the declaration for the suite class
   */
  private String getSuiteClassString(String suiteClassName) {
    ClassSourceBuilder sourceBuilder = new ClassSourceBuilder(suiteClassName, packageName);

    List<String> imports = new ArrayList<>();
    imports.add("org.junit.runner.RunWith;");
    imports.add("org.junit.runners.Suite;");
    sourceBuilder.addImports(imports);

    List<String> annotations = new ArrayList<>();
    annotations.add("@RunWith(Suite.class)");
    annotations.add("@Suite.SuiteClasses({");

    List<String> testClassNames = getTestClassNames();
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
   * writeDriverFile writes non-reflective driver for tests as a main class. The
   * file is written to the directory pointed to by writer object in a class
   * whose name is the {@link #masterTestClassName}.
   *
   * @return {@link File} object for generated Java file.
   */
  public File writeDriverFile() {
    File dir = this.getDir();
    String driverName = masterTestClassName + "Driver";
    File file = new File(dir, driverName + ".java");

    List<String> testClassNames = getTestClassNames();

    String driverClassString = createTestDriverString(driverName, testClassNames);

    try (PrintStream out = createTextOutputStream(file)) {
      out.println(driverClassString);
    }
    return file;
  }

  /**
   * Create non-reflective test driver as a main class.
   *
   * @param driverName  the name for the driver class
   * @param testClassNames  the names for the test classes
   * @return the test driver class as a {@code String}
   */
  private String createTestDriverString(String driverName, List<String> testClassNames) {
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
        methodSourceBuilder.addBodyText("  " + testVariable + "." + methodName + "();");
        methodSourceBuilder.addBodyText("} catch (Throwable e) {");
        methodSourceBuilder.addBodyText("  wasSuccessful = false;");
        methodSourceBuilder.addBodyText("  e.printStackTrace();");
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
    methodSourceBuilder.addBodyText("  System.exit(1);");
    methodSourceBuilder.addBodyText("}");
    String methodBody = methodSourceBuilder.toString();
    sourceBuilder.addMember(methodBody);
    return sourceBuilder.toString();
  }

  /**
   * Returns the number of digits in the printed representation of the argument.
   *
   * @param n  the number
   * @return the number of digits in string form of given number
   */
  private int numDigits(int n) {
    return (int) Math.log10(n) + 1;
  }

  /*
   * A NameGenerator generates a sequence of names as strings in the form
   * "prefix"+i for integer i. Pads the counter with zeros to ensure a minimum
   * number of digits determined by field digits.
   */
  private class NameGenerator {

    private int initialValue;
    private int counter;
    private String format;

    /*
     * Creates an instance that generates names beginning with prefix, count
     * starting at the initialValue, and 0-padded to digits digits.
     *
     * @param prefix a string to be used as the prefix for all generated names
     *
     * @param initialValue integer starting value for name counter
     *
     * @param digits the minimum number of digits (determines 0-padding)
     */
    NameGenerator(String prefix, int initialValue, int digits) {
      this.initialValue = initialValue;
      this.counter = initialValue;

      this.format = prefix + "%d";
      if (digits > 0) {
        this.format = prefix + "%0" + digits + "d";
      }
      // this.prefix = prefix;
      // this.digits = digits;
    }

    /*
     * Generates names without 0-padding on counter.
     *
     * @param prefix is a string to be used as a prefix for all names generated
     */
    NameGenerator(String prefix) {
      this(prefix, 0, 0);
    }

    public String next() {
      String name = String.format(format, counter);
      counter++;
      return name;
    }

    int nameCount() {
      return counter - initialValue;
    }
  }

  private void createOutputDir() {
    File dir = getDir();
    if (!dir.exists()) {
      boolean success = dir.mkdirs();
      if (!success) {
        throw new Error("Unable to create directory: " + dir.getAbsolutePath());
      }
    }
  }

  private File getDir() {
    File dir;
    if (dirName == null || dirName.length() == 0) dir = new File(System.getProperty("user.dir"));
    else dir = new File(dirName);
    if (packageName == null) {
      return dir;
    }

    if (packageName.length() == 0) return dir;
    String[] split = packageName.split("\\.");
    for (String s : split) {
      dir = new File(dir, s);
    }
    return dir;
  }

  private static PrintStream createTextOutputStream(File file) {
    try {
      return new PrintStream(file);
    } catch (IOException e) {
      Log.out.println("Exception thrown while creating text print stream:" + file.getName());
      e.printStackTrace();
      System.exit(1);
      throw new Error("This can't happen");
    }
  }
}
