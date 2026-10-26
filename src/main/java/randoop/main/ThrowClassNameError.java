package randoop.main;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * ThrowClassNameError is used to handle a class name error by throwing a {@link
 * RandoopClassNameError} with the message.
 */
public class ThrowClassNameError implements ClassNameErrorHandler {

  /** Creates a ThrowClassNameError. */
  public ThrowClassNameError() {}

  @Override
  public void handle(String className) throws RandoopClassNameError {
    handle(className, null);
  }

  @Override
  public void handle(String className, @Nullable Throwable e) throws RandoopClassNameError {
    if (e != null) {
      throw new RandoopClassNameError(
          className, "Unable to load class \"" + className + "\" due to exception: " + e, e);
    }
    throw new RandoopClassNameError(
        className, "No class with name \"" + className + "\" found on the classpath");
  }
}
