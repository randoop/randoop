package randoop.main;

/** A ClassNameErrorHandler determines the error behavior when a class name error occurs. */
public interface ClassNameErrorHandler {

  /**
   * Performs error handling behavior for bad class name.
   *
   * @param className the name of the class for inclusion in messages
   * @throws RandoopClassNameError if this handler reports the error by throwing an exception
   */
  void handle(String className) throws RandoopClassNameError;

  /**
   * Performs error handling behavior for failure to read class due to exception.
   *
   * @param classname the class name to include in message
   * @param e the exception from loading class
   * @throws RandoopClassNameError if this handler reports the error by throwing an exception
   */
  void handle(String classname, Throwable e) throws RandoopClassNameError;
}
