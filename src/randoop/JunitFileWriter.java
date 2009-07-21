package randoop;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import randoop.util.CollectionsExt;
import randoop.util.Log;

/**
 * Outputs a collection of sequences as Java files, using the JUnit framework, with one method per sequence.
 */
public class JunitFileWriter {

  // The class of the main JUnit suite, and the prefix of the subsuite names.
  private String junitDriverClassName;
  
   // The package name of the main JUnit suite
  private String packageName;
  
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

  /** Creates Junit tests for the faults.
   * Output is a set of .java files.
   */
  public List<File> createJunitTestFiles(List<ExecutableSequence> sequences, String junitTestsClassName) {
    if (sequences.size() == 0) {
      System.out.println("No sequences given to createJunitFiles. No Junit class created.");
      return new ArrayList<File>();
    }
    
    // Create the output directory.
    File dir = getDir();
    if (!dir.exists()) {
      boolean success = dir.mkdirs();
      if (!success) {
        throw new Error("Unable to create directory: " + dir.getAbsolutePath());
      }
    }
    
    List<File> ret = new ArrayList<File>();
    List<List<ExecutableSequence>> subSuites = CollectionsExt.<ExecutableSequence>chunkUp(new ArrayList<ExecutableSequence> (sequences), testsPerFile);
    for (int i = 0 ; i < subSuites.size() ; i++) {
      ret.add(writeSubSuite(subSuites.get(i), i, junitTestsClassName));            
    }
    createdSequencesAndClasses.put(junitTestsClassName, subSuites);
    return ret;
  }

  /** Creates Junit tests for the faults.
   * Output is a set of .java files.
   * 
   * the default junit class name is the driver class name + index
   */
  public List<File> createJunitTestFiles(List<ExecutableSequence> sequences) {
    return createJunitTestFiles(sequences, junitDriverClassName);
  }

  /** create both the test files and the drivers for convinience **/
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


  private File writeSubSuite(List<ExecutableSequence> sequencesForOneFile, int i, String junitTestsClassName) {
    String className = junitTestsClassName + i;
    File file = new File(getDir(), className + ".java");
    PrintStream out = createTextOutputStream(file);

    try{
      outputPackageName(out);
      out.println();
      out.println("import junit.framework.*;");
      out.println();
      out.println("public class " + className + " extends TestCase {");
      out.println();
      writeMain(out, className);
      out.println();
      int testCounter = 1;
      for (ExecutableSequence fault : sequencesForOneFile) {
        if (includeParseableString) {
          out.println("/*");
          out.println(fault.sequence.toString());
          out.println("*/");
        }
        out.println("  public void test" + testCounter++ + "() throws Throwable {");
        out.println();
        out.println(indent(fault.toCodeString()));
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

  private String indent(String codeString) {
    StringBuilder indented = new StringBuilder();
    String[] lines = codeString.split(Globals.lineSep);
    for (String line : lines) {
      indented.append("    " + line + Globals.lineSep);
    }
    return indented.toString();
  }

  
  private void writeMain(PrintStream out, String className) {
    out.println("  // Runs all the tests in this file.");
    out.println("  public static void main(String[] args) {");
    out.println("    junit.textui.TestRunner.run(" + className + ".class);");
    out.println("  }");
  }

  private void outputPackageName(PrintStream out) {
    boolean isDefaultPackage= packageName.length() == 0;
    if (!isDefaultPackage)
      out.println("package " + packageName + ";");
  }

  public File writeDriverFile() {
    return writeDriverFile(Collections.<Class<?>>emptyList(), junitDriverClassName);
  }

  public File writeDriverFile(List<Class<?>> allClasses) {
    return writeDriverFile(allClasses, junitDriverClassName);
  }
  /** Creates Junit tests for the faults.
   * Output is a set of .java files.
   * 
   * @param allClasses List of all classes of interest (this is a workaround for emma missing problem: 
   * we want to compute coverage over all classes, not just those that happened to have been touched during execution.
   * Otherwise, a bad suite can report good coverage.
   * The trick is to insert code that will load all those classes; 
   */    
  public File writeDriverFile(List<Class<?>> allClasses, String driverClassName) {
    File file = new File(getDir(), driverClassName + ".java");
    PrintStream out = createTextOutputStream(file);
    try {
      outputPackageName(out);
      out.println("import junit.framework.*;");
      out.println("import junit.textui.*;");
      out.println("");
      out.println("public class " + driverClassName + " extends TestCase {");
      out.println("");
      out.println("  public static void main(String[] args) {");
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
      for(String junitTestsClassName : createdSequencesAndClasses.keySet()) {
        int numSubSuites = createdSequencesAndClasses.get(junitTestsClassName).size();
        for (int i = 0; i < numSubSuites; i++)
          out.println("    result.addTest(new TestSuite(" + junitTestsClassName + i
              + ".class));");
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

  private File getDir() {
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
  
  private PrintStream createTextOutputStream(File file) {
    try {
      return new PrintStream(file);
    } catch (IOException e) {
      Log.out.println("Exception thrown while creating text print stream:" + file.getName());
      e.printStackTrace();
      System.exit(1);
      return null;//make compiler happy
    }
  }
}
