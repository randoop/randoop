package randoop.sequence;

import java.util.List;
import randoop.Globals;

public class SequenceParseException extends Exception {

  private static final long serialVersionUID = 1L;
  private final String message;

  public SequenceParseException(String msg, List<String> statements, int statementCount) {

    StringBuilder b = new StringBuilder();
    b.append(
        "Error while parsing the following list of strings as a sequence (error was at index "
            + statementCount
            + "): ");
    b.append(msg);
    b.append("").append(Globals.lineSep).append(Globals.lineSep);
    b.append(" While parsing the following sequence:").append(Globals.lineSep);
    for (int i = 0; i < statements.size(); i++) {
      if (i == statementCount) {
        b.append(">> " + statements.get(i) + "").append(Globals.lineSep);
      } else {
        b.append("   " + statements.get(i) + "").append(Globals.lineSep);
      }
    }
    b.append("").append(Globals.lineSep).append(Globals.lineSep);
    this.message = b.toString();
  }

  @Override
  public String getMessage() {
    return message;
  }
}
