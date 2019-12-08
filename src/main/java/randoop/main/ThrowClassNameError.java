package randoop.main;

/**
 * ThrowClassNameError is used to handle a class name error by throwing an {@code Error} with the
 * message.
 */
public class ThrowClassNameError implements ClassNameErrorHandler {

  @Override
  public void handle(String className) {
    handle(className, null);
  }

  @Override
  public void handle(String className, Throwable e) {
    if (e != null) {
      throw new RandoopClassNameError(
          className, "Unable to load class \"" + className + "\" due to exception: " + e, e);
    }
    throw new RandoopClassNameError(
        className, "No class with name \"" + className + "\" found on the classpath");
  }
}
