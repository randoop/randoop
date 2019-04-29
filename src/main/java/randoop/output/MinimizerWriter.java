package randoop.output;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import randoop.main.Minimize;
import randoop.main.RandoopBug;

/**
 * A {@link CodeWriter} that, for an error-revealing test class, writes both the original and
 * minimized class. Minimizes the methods of the test class using {@link Minimize#mainMinimize(Path,
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
  public Path writeClassCode(String packageName, String classname, String classCode)
      throws RandoopOutputException {

    // Write the original class.
    Path testFile = javaFileWriter.writeClassCode(packageName, classname, classCode);

    // Minimize the error-revealing test that has been output.
    try {
      Minimize.mainMinimize(
          testFile,
          // Minimize.testsuitetimeout is set only if the main Randoop command is "minimize".
          System.getProperty("java.class.path"),
          Minimize.testsuitetimeout,
          Minimize.verboseminimizer);
    } catch (IOException e) {
      throw new RandoopOutputException(e);
    }

    Path minimizedFile = Minimize.minimizedFile(testFile);
    try {
      System.out.printf("Renaming %s to %s%n", testFile, minimizedFile);
      Files.move(minimizedFile, testFile, REPLACE_EXISTING);
    } catch (IOException e) {
      System.out.printf("Renaming failed!%n");
      e.printStackTrace();
      throw new RandoopBug(
          String.format("Problem while renaming %s to %s", testFile, minimizedFile), e);
    }

    return testFile;
  }

  @Override
  public Path writeUnmodifiedClassCode(String packageName, String classname, String classCode)
      throws RandoopOutputException {
    return javaFileWriter.writeClassCode(packageName, classname, classCode);
  }
}
