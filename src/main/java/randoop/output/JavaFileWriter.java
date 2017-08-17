package randoop.output;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * A {@link CodeWriter} that writes JUnit4 test class source text to a {@code .java} file with
 * annotations so that tests are executed in ascending alphabetical order by test method name.
 */
public class JavaFileWriter implements CodeWriter {

  /** The directory to which JUnit files are written. */
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
   * writeClassCode writes a code sequence as a JUnit4 test class to a .java file. Tests are
   * executed in ascending alphabetical order by test method name.
   *
   * @param packageName the package name for the class
   * @param className the name of the class
   * @param classCode the source text of the test class
   * @return the File object for generated java file
   */
  @Override
  public File writeClassCode(String packageName, String className, String classCode)
      throws RandoopOutputException {
    File dir = createOutputDir(packageName);
    File file = new File(dir, className + ".java");

    try (PrintWriter out = new PrintWriter(file, UTF_8.name())) {
      out.println(classCode);
    } catch (IOException e) {
      String message = "Exception creating print writer for file " + file.getName();
      throw new RandoopOutputException(message, e);
    }

    return file;
  }

  @Override
  public File writeUnmodifiedClassCode(String packageName, String classname, String classCode)
      throws RandoopOutputException {
    return writeClassCode(packageName, classname, classCode);
  }

  /**
   * Create the output directory for the package if it does not already exist.
   *
   * @param packageName the package name
   * @return the {@code File} for the created directory
   * @throws RandoopOutputException if the directory for the package could not be created
   */
  private File createOutputDir(String packageName) throws RandoopOutputException {
    File dir = getDir(packageName);
    if (!dir.exists()) {
      boolean success = dir.mkdirs();
      if (!success) {
        throw new RandoopOutputException("Unable to create directory: " + dir.getAbsolutePath());
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
}
