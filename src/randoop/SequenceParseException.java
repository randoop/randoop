package randoop;

import java.util.List;

public class SequenceParseException extends Exception {
  
  private static final long serialVersionUID = 1L;
  private final String message;

  public SequenceParseException(String msg, List<String> statements, int statementCount) {
    
    StringBuilder b = new StringBuilder();
    b.append("Error while parsing the following list of strings as a sequence (error was at index " + statementCount + "): ");
    b.append(msg);
    b.append("\n\n");
    b.append(" While parsing the following sequence:\n");
    for (int i = 0 ; i < statements.size() ; i++) {
      if (i == statementCount) {
        b.append(">> " + statements.get(i) + "\n");
      } else {
        b.append("   " + statements.get(i) + "\n");
      }
    }
    b.append("\n\n");
    this.message = b.toString();
  }
  
  public String getMessage() {
    return message;
  }

}
