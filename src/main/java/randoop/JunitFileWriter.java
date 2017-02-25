package randoop;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.zip.GZIPOutputStream;

import com.google.common.base.Charsets;
import com.google.common.io.CharSink;
import randoop.main.GenAllTests;
import randoop.main.GenInputsAbstract;
import randoop.sequence.ExecutableSequence;
import randoop.util.Log;

/**
 * JunitFileWriter is a class that for a collection of sequences, outputs Java files containing one
 * JUnit4 test method per sequence. An object manages the information for a suite of tests (name,
 * package, and directory) and is used by first running writeJUnitTestFiles and then writeSuiteFile
 * or writeDriverFile.
 */
public class JunitFileWriter {

  /**
   * The JUnit annotation for the BeforeAll option
   */
  private static final String BEFORE_ALL = "@BeforeClass";
  /**
   * The JUnit annotation for the AfterAll option
   */
  private static final String AFTER_ALL = "@AfterClass";
  /**
   * The JUnit annotation for the BeforeEach option
   */
  private static final String BEFORE_EACH = "@Before";
  /**
   * The JUnit annotation for the AfterEach option
   */
  private static final String AFTER_EACH = "@After";
  /**
   * The method name for the BeforeAll option
   */
  private static final String BEFORE_ALL_METHOD = "setupAll";
  /**
   * The method name for the AfterAll option
   */
  private static final String AFTER_ALL_METHOD = "teardownAll";
  /**
   * The method name for the BeforeEach option
   */
  private static final String BEFORE_EACH_METHOD = "setup";
  /**
   * The method name for the AfterEach option
   */
  private static final String AFTER_EACH_METHOD = "teardown";
  // The class of the main JUnit suite, and the prefix of the subsuite names.
  private final String masterTestClassName;
  // The package name of the main JUnit suite
  private final String packageName;
  // The directory where the JUnit files should be written to.
  private final String dirName;
  /**
   * writeJUnitTestFiles writes a suite of test class files from a list of lists of executable
   * sequences. Each executable sequence corresponds to a test method, a list of executable
   * sequences corresponds to a test class, and the list of lists to a test suite.
   *
   * @param seqPartition suite of test classes as a list of lists of executable sequences
   * @return list of File objects corresponding to test class files generated
   * @see #writeSuiteFile
   * @see #writeDriverFile
   */
  NameGenerator classNameGen;
  /**
   * testClassCount indicates the number of test classes written for the code partitions received by
   * writeJUnitTestFiles. It is used to generate the list of test class names.
   */
  private int testClassCount = 0;
  /**
   * classMethodCounts maps test class names to the number of methods in each class. This is used to
   * generate lists of method names for a class, since current convention is that a test method is
   * named "test"+i for some integer i.
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

  /**
   * JunitFileWriter creates an instance of class holding information needed to write a test suite.
   *
   * @param junitDirName directory where files are to be written
   * @param packageName package name to be used in JUnit test classes
   * @param masterTestClassName name of test class suite/driver
   */
  public JunitFileWriter(String junitDirName, String packageName, String masterTestClassName) {
    this.dirName = junitDirName;
    this.packageName = packageName;
    this.masterTestClassName = masterTestClassName;
  }

  // TODO document and move to util directory.
  private static String indent(String codeString) {
    StringBuilder indented = new StringBuilder();
    String[] lines = codeString.split(Globals.lineSep);
    for (String line : lines) {
      indented.append("    ").append(line).append(Globals.lineSep);
    }
    return indented.toString();
  }

  private static void outputPackageName(PrintStream out, String packageName) {
    boolean isDefaultPackage = packageName.length() == 0;
    if (!isDefaultPackage) out.println("package " + packageName + ";");
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

  private static FileOutputStream createTextOutputStream(File file, boolean append) {
    try {
      return new FileOutputStream(file, append);
    } catch (IOException e) {
      Log.out.println("Exception thrown while creating text print stream:" + file.getName());
      e.printStackTrace();
      System.exit(1);
      throw new Error("This can't happen");
    }
  }

  /**
   * Add text for BeforeClass-annotated method in each generated test class.
   *
   * @param text the (Java) text for method
   */
  public void addBeforeAll(List<String> text) {
    this.beforeAllText = text;
  }

  /**
   * Add text for AfterClass-annotated method in each generated text class.
   *
   * @param text the (Java) text for method
   */
  public void addAfterAll(List<String> text) {
    this.afterAllText = text;
  }

  /**
   * Add text for Before-annotated method in each generated test class.
   *
   * @param text the (Java) text for method
   */
  public void addBeforeEach(List<String> text) {
    this.beforeEachText = text;
  }

  /**
   * Add text for After-annotated method in each generated test class.
   *
   * @param text the (Java) text for method
   */
  public void addAfterEach(List<String> text) {
    this.afterEachText = text;
  }

  /*
   *
   */

  public List<File> writeJUnitTestFiles(List<List<ExecutableSequence>> seqPartition) {
    List<File> ret = new ArrayList<>();

    if (classNameGen == null) {
      classNameGen = new NameGenerator(masterTestClassName);
    }

    createOutputDir();

    for (List<ExecutableSequence> partition : seqPartition) {
      ret.add(writeTestClass(partition, classNameGen.next()));
    }

    testClassCount = classNameGen.nameCount();

    return ret;
  }

  /**
   * writeTestClass writes a code sequence as a JUnit4 test class to a .java file. Tests are
   * executed in ascending alphabetical order by test method name.
   *
   * @param sequences list of executable sequences for method bodies
   * @param testClassName name of test class
   * @return the File object for generated java file
   */
  private File writeTestClass(List<ExecutableSequence> sequences, String testClassName) {

    File file = new File(getDir(), testClassName + ".java");
    String mainCUT = GenAllTests.testclass.get(0);
    if (mainCUT.indexOf(".") >= 0) {
      mainCUT = mainCUT.substring(mainCUT.lastIndexOf("."));
    }
    File allSequencesFile =
        new File(
            getDir(), mainCUT + "_" + mainCUT + "_max_size_" + GenInputsAbstract.maxsize + ".txt");

    PrintStream out = createTextOutputStream(file);
    FileOutputStream outSequences;

    NameGenerator methodNameGen = new NameGenerator("test", 1, numDigits(sequences.size()));

    try {
      outputPackageName(out, packageName);
      out.println();
      if (afterEachText != null) {
        out.println("import org.junit.After;");
      }
      if (afterAllText != null) {
        out.println("import org.junit.AfterClass;");
      }
      if (beforeEachText != null) {
        out.println("import org.junit.Before;");
      }
      if (beforeAllText != null) {
        out.println("import org.junit.BeforeClass;");
      }
      out.println("import org.junit.FixMethodOrder;");
      out.println("import org.junit.Test;");
      out.println("import org.junit.runners.MethodSorters;");
      out.println();
      out.println("@FixMethodOrder(MethodSorters.NAME_ASCENDING)");
      out.println("public class " + testClassName + " {");
      out.println();
      out.println("  public static boolean debug = false;");
      out.println();

      if (beforeAllText != null) {
        writeFixture(out, BEFORE_ALL, "static", BEFORE_ALL_METHOD, beforeAllText);
      }
      if (afterAllText != null) {
        writeFixture(out, AFTER_ALL, "static", AFTER_ALL_METHOD, afterAllText);
      }
      if (beforeEachText != null) {
        writeFixture(out, BEFORE_EACH, "", BEFORE_EACH_METHOD, beforeEachText);
      }
      if (afterEachText != null) {
        writeFixture(out, AFTER_EACH, "", AFTER_EACH_METHOD, afterEachText);
      }

      for (ExecutableSequence s : sequences) {
        writeTest(out, testClassName, methodNameGen.next(), s);
        String cutSequence = getSequenceOfCallsOnCut(s, false);
        if (allSequencesFile.exists()) {
          outSequences = createTextOutputStream(allSequencesFile, true);
        } else {
          outSequences = createTextOutputStream(allSequencesFile, false);
        }
        PrintStream psSeq = new PrintStream(outSequences);
        psSeq.println(cutSequence);
        out.println();
      }

      out.println("}");
      classMethodCounts.put(testClassName, methodNameGen.nameCount());

      // Serialize sequences
      //      try (FileOutputStream fileos =
      //              new FileOutputStream(new File(getDir(), testClassName + "_serialized.gz"))) {
      //        ObjectOutputStream objectos = new ObjectOutputStream(new GZIPOutputStream(fileos));
      //        objectos.writeObject(new ArrayList<>(sequences));
      //        objectos.close();
      //        fileos.close();
      //      } catch (FileNotFoundException e) {
      //        e.printStackTrace();
      //      } catch (IOException e) {
      //        e.printStackTrace();
      //      }

    } finally {
      if (out != null) out.close();
    }

    return file;
  }

  /**
   * Writes a single text fixture to the output stream.
   *
   * @param out the output stream for writing test class
   * @param annotation the fixture annotation
   * @param modifier text prefix for method declaration
   * @param methodName the method name for fixture method
   * @param bodyText the text of the fixture method
   */
  private void writeFixture(
      PrintStream out,
      String annotation,
      String modifier,
      String methodName,
      List<String> bodyText) {
    String indent = "  ";
    out.println(indent + annotation);
    out.print(indent + "public ");
    if (!modifier.isEmpty()) {
      out.print(modifier + " ");
    }
    out.println("void " + methodName + "() {");
    for (String line : bodyText) {
      out.println(indent + indent + line);
    }
    out.println(indent + "}");
    out.println();
  }

  /**
   * Writes a test method to the output stream for the sequence s.
   *
   * @param out the output stream for test class file
   * @param className the name of test class
   * @param methodName the name of test method
   * @param s the {@link ExecutableSequence} for test method.
   */
  private void writeTest(
      PrintStream out, String className, String methodName, ExecutableSequence s) {

    out.println("  @Test");
    out.println("  public void " + methodName + "() throws Throwable {");
    out.println();
    out.println(
        indent(
            "if (debug) { System.out.format(\"%n%s%n\",\""
                + className
                + "."
                + methodName
                + "\"); }"));
    out.println(indent(s.toCodeString()));

    String cutSequence = getSequenceOfCallsOnCut(s);
    String cutShort = getSequenceOfCallsOnCut(s, false);
    out.println((indent("// CUT WITH FULLY QUALIFIED OPS: " + cutSequence)));
    out.println((indent("// CUT WITH SINGLE NAMES: " + cutShort)));

    out.println("  }");
  }

  private String getSequenceOfCallsOnCut(ExecutableSequence s) {
    return getSequenceOfCallsOnCut(s, true);
  }

  private String getSequenceOfCallsOnCut(ExecutableSequence s, boolean fullOperationName) {
    int cutConstructionIndex;
    String tracedVariable = "";

    for (cutConstructionIndex = 0;
        cutConstructionIndex < s.sequence.size();
        cutConstructionIndex++) {
      if (s.sequence.getStatement(cutConstructionIndex).isConstructorCall()) {
        String cs = s.statementToCodeString(cutConstructionIndex);
        // Find the variable name between blank space and equals sign, given the pattern type var = <value>';
        tracedVariable = cs.substring(cs.indexOf(" ") + 1, cs.indexOf("=") - 1).trim();
        break;
      }
    }

    String cutSequence =
        s.sequence.getStatement(cutConstructionIndex).getOperation().toParsableString() + ";";

    for (int i = cutConstructionIndex; i < s.sequence.size(); i++) {
      if (s.statementToCodeString(i).indexOf(tracedVariable + ".") >= 0) {
        if (fullOperationName) {
          cutSequence += s.sequence.getStatement(i).getOperation().toParsableString() + ";";
        } else {
          cutSequence += s.sequence.getStatement(i).getOperation().getName() + ";";
        }
      }
    }
    if (cutSequence.length() > 1 && cutSequence.lastIndexOf(";") >= 0) {
      cutSequence = cutSequence.substring(0, cutSequence.lastIndexOf(";"));
    }
    return cutSequence;
  }

  /**
   * Generates the list of test class names for previously generated test suites.
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
   * Writes a JUnit4 suite consisting of test classes from {@link #writeJUnitTestFiles(List)} and
   * additional classes provided as a parameter. The file is written to the directory pointed to by
   * writer object in a class whose name is the {@link #masterTestClassName}.
   *
   * @return {@link File} object for test suite file.
   */
  public File writeSuiteFile() {
    File dir = this.getDir();
    String suiteClassName = masterTestClassName;
    File file = new File(dir, suiteClassName + ".java");

    List<String> testClassNames = getTestClassNames();

    try (PrintStream out = createTextOutputStream(file)) {
      outputPackageName(out, packageName);

      out.println();
      out.println("import org.junit.runner.RunWith;");
      out.println("import org.junit.runners.Suite;");
      out.println();
      out.println("@RunWith(Suite.class)");
      out.println("@Suite.SuiteClasses({");

      /*
       * should be done with a joiner - waiting until we can graduate to Java 8
       */
      Iterator<String> testIterator = testClassNames.iterator();
      if (testIterator.hasNext()) {
        out.print(testIterator.next() + ".class");
        while (testIterator.hasNext()) {
          out.println(", ");
          out.print(testIterator.next() + ".class");
        }
        out.println();
      }

      out.println("})");
      out.println("public class " + suiteClassName + "{ }");
    }
    return file;
  }

  /**
   * writeDriverFile writes non-reflective driver for tests as a main class. The file is written to
   * the directory pointed to by writer object in a class whose name is the {@link
   * #masterTestClassName}.
   *
   * @return {@link File} object for generated Java file.
   */
  public File writeDriverFile() {
    File dir = this.getDir();
    File file = new File(dir, masterTestClassName + "Driver.java");

    List<String> testClassNames = getTestClassNames();

    try (PrintStream out = createTextOutputStream(file)) {
      outputPackageName(out, packageName);

      out.println();
      out.println("public class " + masterTestClassName + "Driver {");
      out.println();
      out.println("  public static void main(String[] args) {");

      out.println("    boolean wasSuccessful = true;");

      NameGenerator instanceNameGen = new NameGenerator("t");
      for (String testClass : testClassNames) {

        if (beforeAllText != null) {
          out.println();
          out.println("    " + testClass + "." + BEFORE_ALL_METHOD + "();");
          out.println();
        }

        String testVariable = instanceNameGen.next();
        out.println("    " + testClass + " " + testVariable + "= new " + testClass + "();");

        int classMethodCount = classMethodCounts.get(testClass);
        NameGenerator methodGen = new NameGenerator("test", 1, numDigits(classMethodCount));

        while (methodGen.nameCount() < classMethodCount) {
          out.println();
          if (beforeEachText != null) {
            out.println("    " + testVariable + "." + BEFORE_EACH_METHOD + "();");
          }
          String methodName = methodGen.next();
          out.println("    try {");
          out.println("      " + testVariable + "." + methodName + "();");
          out.println("    } catch (Throwable e) {");
          out.println("      wasSuccessful = false;");
          out.println("      e.printStackTrace();");
          out.println("    }");
          if (afterEachText != null) {
            out.println("    " + testVariable + "." + AFTER_EACH_METHOD + "();");
          }
        }

        if (afterAllText != null) {
          out.println("    " + testClass + "." + AFTER_ALL_METHOD + "();");
        }

        out.println();
      }

      out.println("    if ( !wasSuccessful ) {");
      out.println("      System.exit(1);");
      out.println("    }");
      out.println("  }");
      out.println();
      out.println("}");
    }
    return file;
  }

  /**
   * Returns the number of digits in the printed representation of the argument.
   *
   * @param n the number
   * @return the number of digits in string form of given number
   */
  private int numDigits(int n) {
    return (int) Math.log10(n) + 1;
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
}
