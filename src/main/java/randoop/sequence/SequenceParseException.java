package randoop.sequence;

import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;
import randoop.Globals;

/** Exception thrown when a sequence cannot be parsed. */
public class SequenceParseException extends Exception {

  /** ID for serializing this class. */
  private static final long serialVersionUID = 1L;

  /** The error message for the parse exception. */
  private final String message;

  public SequenceParseException(@Nullable String msg, List<String> statements, int statementCount) {

    StringBuilder b = new StringBuilder(256);
    b.append(
        "Error while parsing the following list of strings as a sequence (error was at index "
            + statementCount
            + "): ");
    if (msg != null) {
      b.append(msg);
    }
    b.append("").append(Globals.lineSep).append(Globals.lineSep);
    b.append(" While parsing the following sequence:").append(Globals.lineSep);
    for (int i = 0; i < statements.size(); i++) {
      if (i == statementCount) {
        b.append(">> ");
        b.append(statements.get(i)).append(Globals.lineSep);
      } else {
        b.append("   ");
        b.append(statements.get(i)).append(Globals.lineSep);
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
