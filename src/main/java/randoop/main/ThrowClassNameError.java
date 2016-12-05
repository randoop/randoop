package randoop.main;

/**
 * ThrowClassNameError is used to handle a class name error by throwing an
 * {@code Error} with the message.
 */
public class ThrowClassNameError implements ClassNameErrorHandler {

  @Override
  public void handle(String className) {
    throw new RandoopClassNameError(
        "No class with name \"" + className + "\" found on the classpath");
  }

  @Override
  public void handle(String classname, Throwable e) {
    if (e != null) {
      throw new RandoopClassNameError(
          "Unable to load class \"" + classname + "\" due to exception: " + e);
    }
    throw new RandoopClassNameError(
        "No class with name \"" + classname + "\" found on the classpath");
  }
}
