package randoop;

// Not meant for serialization.
public class StatementKindParseException extends Exception {

  private static final long serialVersionUID = 1L;

  public StatementKindParseException(String string) {
    super(string); 
  }

}
