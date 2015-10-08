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
import randoop.util.CollectionsExt;
import randoop.util.Log;
import randoop.experimental.SequencePrettyPrinter;
import randoop.main.GenInputsAbstract;

/**
 * Outputs a collection of sequences as Java files, using the JUnit framework, with one method per sequence.
 */
public class JunitFileWriter {

  // The class of the main JUnit suite, and the prefix of the subsuite names.
  public final String masterTestClassName;

   // The package name of the main JUnit suite
  public final String packageName;

  // The directory where the JUnit files should be written to.
  public final String dirName;

  public static boolean includeParseableString = false;

  private int testsPerFile;

  private Map<String, List<List<ExecutableSequence>>> createdSequencesAndClasses = new LinkedHashMap<String, List<List<ExecutableSequence>>>();

  private Map<String, Integer> methodCountMap = new LinkedHashMap<String,Integer>();

  public JunitFileWriter(String junitDirName, String packageName, String junitDriverClassName, int testsPerFile) {
    this.dirName = junitDirName;
    this.packageName = packageName.trim();
    this.masterTestClassName = junitDriverClassName;
    this.testsPerFile = testsPerFile;
  }
  

  public static File createJunitTestFile(String junitOutputDir, String packageName, ExecutableSequence es, String className) {
    JunitFileWriter writer = new JunitFileWriter(junitOutputDir, packageName, "dummy", 1);
    writer.createOutputDir();
    return writer.writeSubSuite(Collections.singletonList(es), className);
  }
  
  /** Creates Junit tests for the faults.
   * Output is a set of .java files.
   */
  public List<File> createJunitTestFiles(List<ExecutableSequence> sequences, String junitTestsClassName) {
    if (sequences.size() == 0) {
      System.out.println("No tests were created. No JUnit class created.");
      return new ArrayList<File>();
    }

    createOutputDir();

    //partition lists of test sequences to control file size (and bytecode size)
    List<File> ret = new ArrayList<File>();
    List<List<ExecutableSequence>> subSuites = CollectionsExt.<ExecutableSequence>chunkUp(new ArrayList<ExecutableSequence> (sequences), testsPerFile);
    for (int i = 0 ; i < subSuites.size() ; i++) {
      ret.add(writeSubSuite(subSuites.get(i), junitTestsClassName + i));
    }
    createdSequencesAndClasses.put(junitTestsClassName, subSuites);
    return ret;
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

  /** Creates Junit tests for the faults.
   * Output is a set of .java files.
   *
   * the default junit class name is the driver class name + index
   */
  
  public List<File> createJunitTestFiles(List<ExecutableSequence> sequences) {
    return createJunitTestFiles(sequences, masterTestClassName);
  }

  /** create both the test files and the drivers for convenience **/
  public List<File> createJunitFiles(List<ExecutableSequence> sequences) {
    List<File> ret = new ArrayList<File>();
    ret.addAll(createJunitTestFiles(sequences));
    ret.add(writeDriverFile());
    return ret;
  }

  /*
   * Writes a JUnit4 test class for a list of sequences
   */
  private File writeSubSuite(List<ExecutableSequence> sequences, String testClassName) {
    if (GenInputsAbstract.pretty_print) {
      SequencePrettyPrinter printer = new SequencePrettyPrinter(sequences, packageName, testClassName);
      return printer.createFile(getDir().getAbsolutePath());
    }

    String className = testClassName;
    File file = new File(getDir(), className + ".java");
    PrintStream out = createTextOutputStream(file);

    try {
      outputPackageName(out, packageName);
      out.println();
      out.println("import org.junit.Test;");
      out.println();
      out.println("public class " + className + " {");
      out.println();
      out.println("  public static boolean debug = false;");
      out.println();
      int testCounter = 0;
      for (ExecutableSequence s : sequences) {
        if (includeParseableString) {
          out.println("/*");
          out.println(s.sequence.toString());
          out.println("*/");
        }
        testCounter++;
        writeTest(out, className, testCounter, s);
        out.println();
      }
      out.println("}");
      methodCountMap .put(className,testCounter);
    } finally {
      if (out != null)
        out.close();
    }

    return file;
  }

/*
 * Writes a test method to the output stream for the sequence s with name "test"+testCounter. 
 */
  private void writeTest(PrintStream out, String className, int testCounter, ExecutableSequence s) {
    out.println("  @Test");
    out.println("  public void test" + testCounter + "() throws Throwable {");
    out.println();
    out.println(indent("if (debug) { System.out.println(); System.out.print(\"" + className + ".test" + testCounter + "\"); }"));
    out.println();
    out.println(indent(s.toCodeString()));
    out.println("  }");
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
  
  public List<String> getJunitTestSuiteNames() {
    List<String> junitTestSuites = new LinkedList<String>();
    for (String junitTestsClassName : createdSequencesAndClasses.keySet()) {
      int numSubSuites = createdSequencesAndClasses.get(junitTestsClassName).size();
      for (int i = 0; i < numSubSuites; i++) {
        junitTestSuites.add(junitTestsClassName + i);
      }
    } 
    return junitTestSuites;
  }
  
  /*
   * writes JUnit4 suite based on list of class names
   */
  public File writeSuiteFile (List<String> testClassNames) {
    File dir = this.getDir();
    String suiteClassName = masterTestClassName;
    File file = new File(dir, suiteClassName+".java");
    PrintStream out = createTextOutputStream(file);
    try {
      outputPackageName(out,packageName);
      
      out.println();
      out.println("import org.junit.runner.RunWith;");
      out.println("import org.junit.runners.Suite;");
      out.println();
      out.println("@RunWith(Suite.class)");
      out.println("@Suite.SuiteClasses({");
     
      Iterator<String> testIterator = testClassNames.iterator();
      if (testIterator.hasNext()) {
        out.println(testIterator.next() + ".class");
        while (testIterator.hasNext()) {
          out.print(", ");
          out.println(testIterator.next() + ".class");
        }
      }
      
      out.println("})");
      out.println("public class " + suiteClassName + "{ }");
    } finally {
      if (out != null) {
        out.close();
      }
    }
    return file;
  }
  
  /*
   * write non-reflective driver for tests
   * Note that this relies on method names being generated as "test"+i in writeSubSuite
   */
  public File writeDriverFile(List<String> testClassNames) {
    File dir = this.getDir();
    File file = new File(dir, masterTestClassName + "Driver.java");
    PrintStream out = createTextOutputStream(file);
    try {
      outputPackageName(out, packageName);

      out.println();
      out.println("public class " + masterTestClassName + "Driver {");
      out.println();
      out.print("  public static void main(String[] args) {");
      
      if (GenInputsAbstract.init_routine != null) {
        out.println ("    " + GenInputsAbstract.init_routine + "();");
      }
      
      int classIndex = 0; //counter for test class object instances
      for (String testClass : testClassNames) {
        String testVariable = "t"+classIndex;
        out.println(testClass + " " + testVariable + "= new " + testClass + "()");
        
        if (methodCountMap.containsKey(testClass)) {
          int methodCount = methodCountMap.get(testClass);
          for (int methodIndex = 1; methodIndex < methodCount; methodIndex++) {
            String methodName = "test" + methodIndex;
            out.println("try {");
              out.println("  " + testVariable + "." + methodName + "()");
              out.println("} catch (Throwable e) {");
              out.println("  e.printStackTrace();");
              out.println("}");
          }
        }
        
        classIndex++;
        out.println();
      }
      
      out.println("  }");
      out.println();
      out.println("}");
    } finally {
      if (out != null)
        out.close();
    }
    return file;
  }
  
 
  /**
   * Creates Junit tests for the faults.
   * Output is a set of .java files.
   */
  public File writeDriverFile() {
    return writeDriverFile(getJunitTestSuiteNames());
  }

  public File getDir() {
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
