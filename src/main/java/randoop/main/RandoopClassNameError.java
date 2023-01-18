package randoop.main;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.plumelib.reflection.ReflectionPlume;
import randoop.Globals;

/** Exception for classname errors. */
public class RandoopClassNameError extends Error {
  private static final long serialVersionUID = -3625971508842588810L;

  /** The erroneous class name. */
  public String className;

  /**
   * Create a new RandoopClassNameError.
   *
   * @param className the erroneous class name
   * @param message the detail message
   */
  public RandoopClassNameError(String className, String message) {
    this(className, message, null);
  }

  /**
   * Create a new RandoopClassNameError.
   *
   * @param className the erroneous class name
   * @param message the detail message
   * @param cause the underlying exception
   */
  public RandoopClassNameError(String className, String message, @Nullable Throwable cause) {
    super(
        message
            + Globals.lineSep
            + "Classpath:"
            + Globals.lineSep
            + ReflectionPlume.classpathToString(),
        cause);
    this.className = className;
  }
}
