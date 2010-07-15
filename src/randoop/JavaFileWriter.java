package randoop;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.List;

import randoop.util.CollectionsExt;
import randoop.util.Log;

/**
 * Outputs a collection of sequences as Java files, with one method per sequence.
 */
public class JavaFileWriter {

  // Creates Junit tests for the faults.
  // Output is a set of .java files.
  public static void createJavaFiles(List<Sequence> sequences, String junitClassName, int testsPerFile) {
    if (sequences.size() == 0) {
      System.out.println("No sequences given to createJavaFiles. No files created.");
      return;
    }

    List<List<Sequence>> subSuites = CollectionsExt.<Sequence>chunkUp(sequences, testsPerFile);
    for (int i = 0 ; i < subSuites.size() ; i++) {
      writeSubSuite(subSuites.get(i), i, junitClassName);            
    }

    writeDriverFile(subSuites.size(), junitClassName);
  }

  private static void writeSubSuite(List<Sequence> sequencesForOneFile, int i, String junitClassName) {
    PrintStream out = createTextOutputStream(junitClassName + i + ".java");
    try{
      out.println("public class " + junitClassName + i + " {");

      int testCounter = 0;
      for (Sequence fault : sequencesForOneFile) {
        out.println("public static void testclasses" + testCounter++ + "() {");
        out.println("try {");
        out.println(fault.toCodeString());
        out.println("} catch (Throwable t) { /* do nothing */ }");
        out.println(junitClassName + ".numTests++;");
        out.println("}");
        out.println();
      }

      out.println("public static void run() {");
      for (int ti = 0 ; ti < testCounter ; ti++) {
        out.println("testclasses" + ti + "();");
      }
      out.println("}");
      out.println("}");
    } finally {
      if (out != null)
        out.close();
    }
  }

  private static void writeDriverFile(int numSubSuites, String junitClassName) {
    PrintStream out = createTextOutputStream(junitClassName + ".java");
    try {
      out.println("public class " + junitClassName + " {");
      out.println("  public static int numTests = 0;");
      out.println("  public static void main(String[] args) {");
      out.println("    long startTime = System.currentTimeMillis();");
      for (int i = 0; i < numSubSuites; i++)
        out.println("    " + junitClassName + i + ".run();");
      out.println("    long stopTime = System.currentTimeMillis();");
      out.println("    System.out.println(\"Executed \" + numTests + \" tests.\");");
      out.println("    System.out.println(\"Time spent executing tests: \" + (stopTime-startTime) + \"ms.\");");
      out.println("  }");
      out.println("");
      out.println("}");
    } finally {
      if (out != null)
        out.close();
    }
  }

  private static PrintStream createTextOutputStream(String fileName) {
    try {
      return new PrintStream(new File(fileName));
    } catch (FileNotFoundException e) {
      Log.out.println("Exception thrown while creating text print stream:" + fileName);
      e.printStackTrace();
      System.exit(1);
      throw new Error("This can't happen");
    }
  }
}
