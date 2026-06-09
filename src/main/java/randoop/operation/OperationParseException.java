package randoop.operation;

// Not meant for serialization.
public class OperationParseException extends Exception {

  /** ID for serializing this class. */
  private static final long serialVersionUID = 1L;

  public OperationParseException(String string) {
    super(string);
  }
}
