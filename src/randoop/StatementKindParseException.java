package randoop;

// Not meant for serialization.
@SuppressWarnings("serial")
public class StatementKindParseException extends Exception {

  public StatementKindParseException(String string) {
    super(string); 
  }

}
