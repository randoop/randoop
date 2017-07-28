package randoop.instrument;

/**
 * Exception class specializing {@code ClassNotFoundException} to represent when the {@code Class}
 * for an argument type of a {@link MethodSignature} is not found during a conversion to a
 * reflection object.
 */
class ArgumentClassNotFoundException extends ClassNotFoundException {
  /**
   * Creates an exception object with the message.
   *
   * @param message the exception message
   */
  ArgumentClassNotFoundException(String message) {
    super(message);
  }
}
