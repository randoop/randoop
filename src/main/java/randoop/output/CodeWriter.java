package randoop.output;

import java.nio.file.Path;

/** Interface for a method {@code writeClassCode} that writes a class to a file. */
public interface CodeWriter {

  /**
   * Writes the given class using this {@link CodeWriter}. May modify the class text before writing
   * it. May write additional files (but those are not returned).
   *
   * @param packageName the package name of the class
   * @param classname the simple name of the class
   * @param classCode the text of a Java class, must be compilable
   * @return the {@code Path} object for the Java file written
   * @throws RandoopOutputException if there is an error while writing the code
   */
  public Path writeClassCode(String packageName, String classname, String classCode)
      throws RandoopOutputException;

  /**
   * Writes the given class. Does not modify the class text.
   *
   * @param packageName the package name of the class
   * @param classname the simple name of the class
   * @param classCode the text of the class to be written, must be compilable
   * @return the {@code Path} object for the Java file written
   * @throws RandoopOutputException if there is an error while writing the code
   */
  public Path writeUnmodifiedClassCode(String packageName, String classname, String classCode)
      throws RandoopOutputException;
}
