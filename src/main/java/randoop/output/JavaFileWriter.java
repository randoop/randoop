package randoop.output;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

/**
 * A {@link CodeWriter} that writes JUnit4 test class source text to a .java file with annotations
 * so that tests are executed in ascending alphabetical order by test method name.
 */
public class JavaFileWriter implements CodeWriter {

  /** The directory to which JUnit files are written */
  private final String dirName;

  /**
   * JavaFileWriter creates an instance of class holding information needed to write a test suite.
   *
   * @param junitDirName directory where files are to be written
   */
  public JavaFileWriter(String junitDirName) {
    this.dirName = junitDirName;
  }

  /**
   * writeClass writes a code sequence as a JUnit4 test class to a .java file. Tests are executed in
   * ascending alphabetical order by test method name.
   *
   * @param packageName the package name for the class
   * @param className the name of the class
   * @param testClassText the source text of the test class
   * @return the File object for generated java file
   */
  @Override
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

  @Override
  public File writeUnmodifiedClass(String packageName, String classname, String classString) {
    return writeClass(packageName, classname, classString);
  }

  /**
   * Create the output directory for the package if it does not already exist.
   *
   * @param packageName the package name
   * @return the {@code File} for the created directory
   */
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

  /**
   * Returns the directory for the package name relative to the directory name of this writer.
   *
   * @param packageName the package name
   * @return the {@code File} for the directory corresponding to the package name
   */
  private File getDir(String packageName) {
    File dir;
    if (dirName == null || dirName.length() == 0) {
      dir = new File(System.getProperty("user.dir"));
    } else {
      dir = new File(dirName);
    }
    if (packageName == null) {
      return dir;
    }

    if (packageName.length() == 0) {
      return dir;
    }
    String[] split = packageName.split("\\.");
    for (String s : split) {
      dir = new File(dir, s);
    }
    return dir;
  }

  /**
   * Returns the output stream to print to the given file.
   *
   * @param file the file to be written to
   * @return the {@code PrintStream} for writing to the file
   */
  private static PrintStream createTextOutputStream(File file) {
    try {
      return new PrintStream(file);
    } catch (IOException e) {
      System.out.println("Exception thrown while creating text print stream:" + file.getName());
      e.printStackTrace();
      System.exit(1);
      throw new Error("This can't happen");
    }
  }
}
