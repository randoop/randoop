package randoop;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import randoop.util.CollectionsExt;
import randoop.util.Log;
import randoop.experimental.SequencePrettyPrinter;
import randoop.main.GenInputsAbstract;

/**
 * Outputs a collection of sequences as Java files, using the JUnit framework, with one method per sequence.
 */
public class JunitFileWriter {

  // The class of the main JUnit suite, and the prefix of the subsuite names.
  public String junitDriverClassName;

   // The package name of the main JUnit suite
  public String packageName;

  // The directory where the JUnit files should be written to.
  private String dirName;

  public static boolean includeParseableString = false;

  private int testsPerFile;

  private Map<String, List<List<ExecutableSequence>>> createdSequencesAndClasses = new LinkedHashMap<String, List<List<ExecutableSequence>>>();

  public JunitFileWriter(String junitDirName, String packageName, String junitDriverClassName, int testsPerFile) {
    this.dirName = junitDirName;
    this.packageName = packageName;
    this.junitDriverClassName = junitDriverClassName;
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
      System.out.println("No sequences given to createJunitFiles. No Junit class created.");
      return new ArrayList<File>();
    }

    createOutputDir();

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
    return createJunitTestFiles(sequences, junitDriverClassName);
  }

  /** create both the test files and the drivers for convenience **/
  public List<File> createJunitFiles(List<ExecutableSequence> sequences, List<Class<?>> allClasses) {
    List<File> ret = new ArrayList<File>();
    ret.addAll(createJunitTestFiles(sequences));
    ret.add(writeDriverFile(allClasses));
    return ret;
  }

  /** create both the test files and the drivers for convinience **/
  public List<File> createJunitFiles(List<ExecutableSequence> sequences) {
    List<File> ret = new ArrayList<File>();
    ret.addAll(createJunitTestFiles(sequences));
    ret.add(writeDriverFile());
    return ret;
  }


  private File writeSubSuite(List<ExecutableSequence> sequencesForOneFile, String junitTestsClassName) {
	if(GenInputsAbstract.pretty_print) {
	  SequencePrettyPrinter printer = new SequencePrettyPrinter(sequencesForOneFile, packageName, junitTestsClassName);
      return printer.createFile(getDir().getAbsolutePath());
	}
	  
    String className = junitTestsClassName;
    File file = new File(getDir(), className + ".java");
    PrintStream out = createTextOutputStream(file);

    try{
      outputPackageName(out, packageName);
      out.println();
      out.println("import junit.framework.*;");
      out.println();
      out.println("public class " + className + " extends TestCase {");
      out.println();
      out.println("  public static boolean debug = false;");
      out.println();
      int testCounter = 1;
      for (ExecutableSequence s : sequencesForOneFile) {
        if (includeParseableString) {
          out.println("/*");
          out.println(s.sequence.toString());
          out.println("*/");
        }
        out.println("  public void test" + testCounter++ + "() throws Throwable {");
        out.println();
        // Replaced this printf by the below to avoid a dependence on Java
        // 5 -- printf was added to the PrintStream class only in J2SE 5.0.
        // out.println(indent("if (debug) System.out.printf(\"%n" + className + ".test" + (testCounter-1) + "\");"));
        out.println(indent("if (debug) { System.out.println(); System.out.print(\"" + className + ".test" + (testCounter-1) + "\"); }"));
        out.println();
        out.println(indent(s.toCodeString()));
        out.println("  }");
        out.println();
      }
      out.println("}");
    } finally {
      if (out != null)
        out.close();
    }

    return file;
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

  public File writeDriverFile() {
    return writeDriverFile(junitDriverClassName);
  }

  public File writeDriverFile(List<Class<?>> allClasses) {
    return writeDriverFile(junitDriverClassName);
  }
  /**
   * Creates Junit tests for the faults.
   * Output is a set of .java files.
   */
  public File writeDriverFile(String driverClassName) {
    return writeDriverFile(getDir(), packageName, driverClassName, getJunitTestSuiteNames());
  }
  
  public List<String> getJunitTestSuiteNames() {
    List<String> junitTestSuites = new LinkedList<String>();
    for(String junitTestsClassName : createdSequencesAndClasses.keySet()) {
      int numSubSuites = createdSequencesAndClasses.get(junitTestsClassName).size();
      for (int i = 0; i < numSubSuites; i++) {
        junitTestSuites.add(junitTestsClassName + i);
      }
    } 
    return junitTestSuites;
  }
  
  public static File writeDriverFile(File dir, String packageName, String driverClassName,
      List<String> junitTestSuiteNames) {
    File file = new File(dir, driverClassName + ".java");
    PrintStream out = createTextOutputStream(file);
    try {
      outputPackageName(out, packageName);
      out.println("import junit.framework.*;");
      out.println("import junit.textui.*;");
      out.println("");
      out.println("public class " + driverClassName + " extends TestCase {");
      out.println("");
      out.println("  public static void main(String[] args) {");
      if (GenInputsAbstract.init_routine != null)
        out.println ("    " + GenInputsAbstract.init_routine + "();");

      out.println("    TestRunner runner = new TestRunner();");
      out.println("    TestResult result = runner.doRun(suite(), false);");
      out.println("    if (! result.wasSuccessful()) {");
      out.println("      System.exit(1);");
      out.println("    }");
      out.println("  }");
      out.println("");
      out.println("  public " + driverClassName + "(String name) {");
      out.println("    super(name);");
      out.println("  }");
      out.println("");
      out.println("  public static Test suite() {");
      out.println("    TestSuite result = new TestSuite();");
      for(String junitTestsClassName : junitTestSuiteNames) {
        out.println("    result.addTest(new TestSuite(" + junitTestsClassName + ".class));");
      }
      out.println("    return result;");
      out.println("  }");
      out.println("");
      out.println("}");
    } finally {
      if (out != null)
        out.close();
    }
    return file;
  }

  public File getDir() {
    File dir = null;
    if (dirName == null || dirName.length() == 0)
      dir = new File(System.getProperty("user.dir"));
    else
      dir = new File(dirName);
    if (packageName == null)
      return dir;
    packageName = packageName.trim(); // Just in case.
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
