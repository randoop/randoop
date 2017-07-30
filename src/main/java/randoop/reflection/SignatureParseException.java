package randoop.reflection;

/** Represents exception when parsing signature strings using {@link SignatureParser}. */
public class SignatureParseException extends Exception {

  private static final long serialVersionUID = 1L;

  public SignatureParseException(String string) {
    super(string);
  }

  public SignatureParseException(String string, Throwable cause) {
    super(string, cause);
  }
}
