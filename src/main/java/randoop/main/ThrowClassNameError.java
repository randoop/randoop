package randoop.main;

/**
 * ThrowClassNameError is used to handle a class name error by throwing an
 * {@code Error} with the message.
 */
public class ThrowClassNameError implements ClassNameErrorHandler {

  @Override
  public void handle(String className) {
    throw new Error("No class found for type name \"" + className + "\"");
  }
}
