package randoop.output;

import java.io.File;
import randoop.main.Minimize;

/**
 * A {@link CodeWriter} that, for an error-revealing test class, writes both the original and
 * minimized class. Minimizes the methods of the test class using {@link Minimize#mainMinimize(File,
 * String, int, boolean)}.
 */
public class MinimizerWriter implements CodeWriter {

  /** The {@link JavaFileWriter} used to write classes. */
  private final JavaFileWriter javaFileWriter;

  /**
   * Creates a {@link MinimizerWriter} using the given {@link JavaFileWriter}.
   *
   * @param javaFileWriter the {@link JavaFileWriter} for writing the classes
   */
  public MinimizerWriter(JavaFileWriter javaFileWriter) {
    this.javaFileWriter = javaFileWriter;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Writes both the original class and the minimized class. Returns the original class.
   */
  @Override
  public File writeClassCode(String packageName, String classname, String classCode)
      throws RandoopOutputException {

    // Write the original class
    File testFile = javaFileWriter.writeClassCode(packageName, classname, classCode);

    // Minimize the error-revealing test that has been output.
    Minimize.mainMinimize(
        testFile, Minimize.suiteclasspath, Minimize.testsuitetimeout, Minimize.verboseminimizer);

    return testFile;
  }

  @Override
  public File writeUnmodifiedClassCode(String packageName, String classname, String classCode)
      throws RandoopOutputException {
    return javaFileWriter.writeClassCode(packageName, classname, classCode);
  }
}
