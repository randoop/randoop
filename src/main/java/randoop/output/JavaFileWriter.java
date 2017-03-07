package randoop.output;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import randoop.util.Log;

public class JavaFileWriter {

  /** The directory to which JUnit files are written */
  private final String dirName;

  /**
   * JavaFileWriter creates an instance of class holding information needed to
   * write a test suite.
   *
   * @param junitDirName
   *          directory where files are to be written
   */
  public JavaFileWriter(String junitDirName) {
    this.dirName = junitDirName;
  }

  /**
   * writeClass writes a code sequence as a JUnit4 test class to a .java
   * file. Tests are executed in ascending alphabetical order by test method
   * name.
   *
   * @param packageName  the package name for the class
   * @param className  the name of the class
   * @param testClassText  the source text of the test class
   * @return the File object for generated java file
   */
  public File writeClass(String packageName, String className, String testClassText) {
    File dir = createOutputDir(packageName);
    File file = new File(dir, className + ".java");
    PrintStream out = createTextOutputStream(file);

    try {
      out.println(testClassText);
    } finally {
      if (out != null) out.close();
    }

    return file;
  }

  private File createOutputDir(String packageName) {
    File dir = getDir(packageName);
    if (!dir.exists()) {
      boolean success = dir.mkdirs();
      if (!success) {
        throw new Error("Unable to create directory: " + dir.getAbsolutePath());
      }
    }
    return dir;
  }

  private File getDir(String packageName) {
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
