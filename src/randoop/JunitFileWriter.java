package randoop;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import randoop.main.GenInputsAbstract;
import randoop.sequence.ExecutableSequence;
import randoop.util.Log;

/**
 * JunitFileWriter is a class that for a collection of sequences, outputs
 * Java files containing one JUnit4 test method per sequence.
 * An object manages the information for a suite of tests (name, package, and directory)
 * and is used by first running writeJUnitTestFiles and then writeSuiteFile or 
 * writeDriverFile. Alternatively, a single test file can be written using the static
 * method writeJUnitTestFile.
 */
public class JunitFileWriter {

  // The class of the main JUnit suite, and the prefix of the subsuite names.
  public final String masterTestClassName;

   // The package name of the main JUnit suite
  public final String packageName;

  // The directory where the JUnit files should be written to.
  public final String dirName;

  public static boolean includeParseableString = false;

  /**
   * testClassCount indicates the number of test classes written for the code partitions
   * received by writeJUnitTestFiles. It is used to generate the list of test class names.
   */
  private int testClassCount = 0;

  /**
   * classMethodCounts maps test class names to the number of methods in each class. 
   * This is used to generate lists of method names for a class, since current convention is that 
   * a test method is named "test"+i for some integer i.
   */
  private Map<String, Integer> classMethodCounts = new LinkedHashMap<String,Integer>();

  /**
   * JunitFileWriter creates an instance of class holding information needed to write
   * a test suite.
   * 
   * @param junitDirName         directory where files are to be written
   * @param packageName          package name to be used in JUnit test classes
   * @param masterTestClassName  name of test class suite/driver
   */
  public JunitFileWriter(String junitDirName, String packageName, String masterTestClassName) {
    this.dirName = junitDirName;
    this.packageName = packageName;
    this.masterTestClassName = masterTestClassName;
  }
  
  //called by PluginBridge.outputSequence
  /**
   * writeJUnitTestFile is a static method that writes a single test class to the
   * specified directory and with the specified class name.
   * 
   * @param junitOutputDir   directory where files are to be written.
   * @param packageName      package name for test classes.
   * @param es               executable sequence to be written to test class.
   * @param className        name of test class.
   * @return File that new test class was written to.
   */
  public static File writeJUnitTestFile(String junitOutputDir, String packageName, ExecutableSequence es, String className) {
    JunitFileWriter writer = new JunitFileWriter(junitOutputDir, packageName, "dummy");
    writer.createOutputDir();
    return writer.writeTestClass(Collections.singletonList(es), className);
  }
  
  /**
   * writeJUnitTestFiles writes a suite of test class files from a list of lists
   * of executable sequences. Each executable sequence corresponds to a test method, a
   * list of executable sequences corresponds to a test class, and the list of lists to a
   * test suite.
   * 
   * @param seqPartition  suite of test classes as a list of lists of executable sequences
   * @return File objects corresponding to test class files generated.
   * 
   * @see writeSuiteFile
   * @see writeDriverFile
   */
  public List<File> writeJUnitTestFiles(List<List<ExecutableSequence>> seqPartition) {
    List<File> ret = new ArrayList<File>();
    
    NameGenerator classNameGen = new NameGenerator(masterTestClassName);
    
    createOutputDir();
  
    for (List<ExecutableSequence> partition : seqPartition) {
      ret.add(writeTestClass(partition, classNameGen.next()));
    }
    
    testClassCount = classNameGen.nameCount();
    
    return ret;
  }

  /**
   * writeTestClass writes a code sequence as a JUnit4 test class to a .java file.
   * Tests are executed in ascending alphabetical order by test method name. 
   * 
   * @param sequences      list of executable sequences for method bodies.
   * @param testClassName  name of test class.
   * @return File object for generated java file.
   */
  private File writeTestClass(List<ExecutableSequence> sequences, String testClassName) {

    String className = testClassName;
    File file = new File(getDir(), className + ".java");
    PrintStream out = createTextOutputStream(file);

    NameGenerator methodNameGen = new NameGenerator("test", 1, numDigits(sequences.size()));
    
    try {
      outputPackageName(out, packageName);
      out.println();
      out.println("import org.junit.FixMethodOrder;");
      out.println("import org.junit.Test;");
      out.println("import org.junit.runners.MethodSorters;");
      out.println();
      out.println("@FixMethodOrder(MethodSorters.NAME_ASCENDING)");
      out.println("public class " + className + " {");
      out.println();
      out.println("  public static boolean debug = false;");
      out.println();
      
      for (ExecutableSequence s : sequences) {
        if (includeParseableString) {
          out.println("/*");
          out.println(s.sequence.toString());
          out.println("*/");
        }
       
        writeTest(out, className, methodNameGen.next(), s);
        out.println();
      }
      out.println("}");
      classMethodCounts.put(className,methodNameGen.nameCount());
    } finally {
      if (out != null)
        out.close();
    }

    return file;
  }

/*
 * 
 */
  /**
   * Writes a test method to the output stream for the sequence s.
   * 
   * @param out  the output stream for test class file.
   * @param className  the name of test class.
   * @param methodName  the name of test method.
   * @param s  the {@link ExecutableSequence} for test method.
   */
  private void writeTest(PrintStream out, String className, String methodName, ExecutableSequence s) {
    out.println("  @Test");
    out.println("  public void " + methodName + "() throws Throwable {");
    out.println();
    out.println(indent("if (debug) { System.out.format(\"%n%s%n\",\"" + className + "." + methodName + "\"); }")); 
    out.println();
    out.println(indent(s.toCodeString()));
    out.println("  }");
  }
  
  /**
   * Generates the list of test class names for previously generated test suite.
   * 
   * @return list of class names.
   */
  private List<String> getTestClassNames() {
    List<String> junitTestSuites = new LinkedList<String>();
    NameGenerator classNameGen = new NameGenerator(masterTestClassName);
    while (classNameGen.nameCount() < testClassCount) {
      junitTestSuites.add(classNameGen.next());
    }
    return junitTestSuites;
  }
  
  /**
   * Writes a JUnit4 suite consisting of test classes
   * from {@link #writeJUnitTestFiles(List)} and additional classes provided as a
   * parameter.
   * The file is written to the directory pointed to by writer object 
   * in a class whose name is the {@link #masterTestClassName}.
   * 
   * @param additionalTestClassNames  a list of class names to be added to suite.
   * @return {@link File} object for test suite file.
   */
  public File writeSuiteFile(List<String> additionalTestClassNames) {
    File dir = this.getDir();
    String suiteClassName = masterTestClassName;
    File file = new File(dir, suiteClassName+".java");
    
    List<String> testClassNames = getTestClassNames();
    if (additionalTestClassNames != null && !additionalTestClassNames.isEmpty() ) {
      testClassNames.addAll(additionalTestClassNames);
    }
    
    try (PrintStream out = createTextOutputStream(file)) {
      outputPackageName(out, packageName);
      
      out.println();
      out.println("import org.junit.runner.RunWith;");
      out.println("import org.junit.runners.Suite;");
      out.println();
      out.println("@RunWith(Suite.class)");
      out.println("@Suite.SuiteClasses({");
 
      /* should be done with a joiner - waiting until we can graduate to Java 8 */
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
   * writeDriverFile writes non-reflective driver for tests as a main class.
   * The file is written to the directory pointed to by writer object 
   * in a class whose name is the {@link #masterTestClassName}.
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
      
      if (GenInputsAbstract.init_routine != null) {
        out.println ("    " + GenInputsAbstract.init_routine + "();");
      }
      
      out.println("    boolean wasSuccessful = true;");
      
      NameGenerator instanceNameGen = new NameGenerator("t");
      for (String testClass : testClassNames) {
        String testVariable = instanceNameGen.next();
        out.println(testClass + " " + testVariable + "= new " + testClass + "()");

        int classMethodCount = classMethodCounts.get(testClass);
        NameGenerator methodGen = new NameGenerator("test", 1, numDigits(classMethodCount));
        
        while ( methodGen.nameCount() < classMethodCount) {
          String methodName = methodGen.next();
          out.println("    try {");
          out.println("      " + testVariable + "." + methodName + "()");
          out.println("    } catch (Throwable e) {");
          out.println("      wasSuccessful = false;");
          out.println("      e.printStackTrace();");
          out.println("    }");
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

  /** Returns the number of digits in the printed representation of the argument. */
  private int numDigits(int n) {
    return (int)Math.log10(n) + 1;
  }
  
  /*
   * A NameGenerator generates a sequence of names as strings in the form "prefix"+i for integer i.
   * Pads the counter with zeros to ensure a minimum number of digits determined by field digits. 
   */
  private class NameGenerator {
    
    private int initialValue;
    private int counter;
    private String format;
    
    /*
     * Creates an instance that generates names beginning with prefix, count starting
     * at the initialValue, and 0-padded to digits digits.
     * 
     * @param prefix a string to be used as the prefix for all generated names.
     * @param initialValue integer starting value for name counter
     * @param digits the minimum number of digits (determines 0-padding)
     */
    public NameGenerator(String prefix, int initialValue, int digits) {
      this.initialValue = initialValue;
      this.counter = initialValue;
      
      this.format = prefix + "%d";
      if (digits > 0) {
        this.format = prefix + "%0" + digits + "d";
      }
      //this.prefix = prefix;
      //this.digits = digits;
    }

    /*
     * Generates names without 0-padding on counter.
     * 
     * @param prefix is a string to be used as a prefix for all names generated.
     */
    public NameGenerator(String prefix) {
      this(prefix, 0, 0);
    }
    
    public String next() {
      String name = String.format(format, counter);
      counter++;
      return name;
    }
    
    public int nameCount() {
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
    File dir = null;
    if (dirName == null || dirName.length() == 0)
      dir = new File(System.getProperty("user.dir"));
    else
      dir = new File(dirName);
    if (packageName == null)
      return dir;
 
    if (packageName.length() == 0)
      return dir;
    String[] split = packageName.split("\\.");
    for (String s : split) {
      dir = new File(dir, s);
    }
    return dir;
  }

  // TODO document and move to util directory.
  public static String indent(String codeString) {
    StringBuilder indented = new StringBuilder();
    String[] lines = codeString.split(Globals.lineSep);
    for (String line : lines) {
      indented.append("    " + line + Globals.lineSep);
    }
    return indented.toString();
  }

  private static void outputPackageName(PrintStream out, String packageName) {
    boolean isDefaultPackage= packageName.length() == 0;
    if (!isDefaultPackage)
      out.println("package " + packageName + ";");
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
