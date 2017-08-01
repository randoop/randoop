package randoop.output;

import java.io.File;

/** Interface for a method {@code writeClassCode} that writes a class to a file. */
public interface CodeWriter {

  /**
   * Writes the given class using this {@link CodeWriter}. May modify the class text before writing
   * it, or write additional files.
   *
   * @param packageName the package name of the class
   * @param classname the name of the class
   * @param classCode the text of a Java class, must be compilable
   * @return the {@code File} object for the Java file written
   */
  public File writeClassCode(String packageName, String classname, String classCode)
      throws RandoopOutputException;

  /**
   * Writes the given class. Does not modify the class text.
   *
   * @param packageName the package name of the class
   * @param classname the name of the class
   * @param classCode the text of the class to be written, must be compilable
   * @return the {@code File} object for the Java file written
   */
  public File writeUnmodifiedClassCode(String packageName, String classname, String classCode)
      throws RandoopOutputException;
}
