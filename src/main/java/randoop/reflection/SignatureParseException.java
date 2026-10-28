package randoop.reflection;

/** Represents exception when parsing signature strings using {@link SignatureParser}. */
public class SignatureParseException extends Exception {

  /** ID for serializing this class. */
  private static final long serialVersionUID = 1L;

  /**
   * Creates a SignatureParseException with the given message.
   *
   * @param string the error message
   */
  public SignatureParseException(String string) {
    super(string);
  }

  /**
   * Creates a SignatureParseException with the given message and cause.
   *
   * @param string the error message
   * @param cause the exception that caused the parse failure
   */
  public SignatureParseException(String string, Throwable cause) {
    super(string, cause);
  }
}
