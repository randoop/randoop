package randoop.output;

import java.io.File;

/** Interface for a method that writes a class to a file. */
public interface CodeWriter {

  /**
   * Writes the given class using this {@link CodeWriter}. May modify the class text, or write
   * additional files.
   *
   * @param packageName the package name of the class
   * @param classname the name of the class
   * @param classCode the text of a Java class, must be compilable
   * @return the {@code File} object for the Java file written
   */
  public File writeClassCode(String packageName, String classname, String classCode);

  /**
   * Writes the given class using this {@link CodeWriter}. Does not modify the class text.
   *
   * @param packageName the package name of the class
   * @param classname the name of the class
   * @param sourceLines the lines of the source text of the class to be written, must be compilable
   * @return the {@code File} object for the Java file written
   */
  public File writeUnmodifiedClassLines(String packageName, String classname, String[] sourceLines);

  /**
   * Writes the given class. Does not modify the class text.
   *
   * @param packageName the package name of the class
   * @param classname the name of the class
   * @param classCode the text of the class to be written, must be compilable
   * @return the {@code File} object for the Java file written
   */
  public File writeUnmodifiedClassCode(String packageName, String classname, String classCode);
}
