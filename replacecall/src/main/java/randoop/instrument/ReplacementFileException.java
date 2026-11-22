package randoop.instrument;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;

/** Exception representing an error in a replacement file. */
class ReplacementFileException extends Exception {

  private static final long serialVersionUID = 20250811;

  /** The name of the replacement file in which the error occurred. */
  private final String filename;

  /** The line of the file where the error occurred. */
  private final int lineNumber;

  /** The text of the line where the error occurred. */
  private final String lineText;

  /**
   * Creates a {@link ReplacementFileException} with the message.
   *
   * @param msg the exception message
   * @param filename the replacement file name
   * @param lineNumber the line of the file where the error occurred
   * @param lineText the text of the line where the error occurred
   */
  /*package*/ ReplacementFileException(
      String msg, String filename, int lineNumber, String lineText) {
    super(msg);
    this.filename = filename;
    this.lineNumber = lineNumber;
    this.lineText = lineText;
  }

  /**
   * Creates a {@link ReplacementFileException} whose message is formed from the messages of {@code
   * cause} and its causes.
   *
   * @param cause the causing exception
   * @param filename the replacement file name
   * @param lineNumber the line of the file where the error occurred
   * @param lineText the text of the line where the error occurred
   */
  /*package*/ ReplacementFileException(
      Throwable cause, String filename, int lineNumber, String lineText) {
    super(null, cause);
    this.filename = filename;
    this.lineNumber = lineNumber;
    this.lineText = lineText;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Returns a string in the format {@code file-name:line-number: message for line: line-text},
   * where the message includes the messages of the causes of this exception.
   */
  @Override
  public String getMessage() {
    return String.format(
        "%s:%d: %s for line: %s", filename, lineNumber, messageWithCauses(), lineText);
  }

  /**
   * Returns the message of this exception, followed by the messages of its causes, separated by ":
   * ". Then come the messages of the exceptions suppressed by any of those exceptions (and of their
   * causes), each introduced by "; also: ". An exception that has no message is represented by its
   * {@code toString()}. A message that already appears in the result is omitted.
   *
   * @return the message of this exception, followed by the messages of its causes and of the
   *     exceptions that they suppress
   */
  private String messageWithCauses() {
    String result = appendMessages(super.getMessage(), getCause(), ": ");
    return result != null ? result : "unknown error";
  }

  /**
   * Appends to {@code result} the messages of {@code first} and its causes, followed by the
   * messages of the exceptions that they suppress.
   *
   * @param result the message so far, or null if there is none
   * @param first the first exception whose message to append, or null if there is none
   * @param separator the separator to put before the message of {@code first}
   * @return {@code result}, extended with the messages
   */
  private static @Nullable String appendMessages(
      @Nullable String result, @Nullable Throwable first, String separator) {
    List<Throwable> suppressed = new ArrayList<>();
    for (Throwable t = first; t != null; t = t.getCause()) {
      String message = t.getMessage();
      if (message == null) {
        message = t.toString();
      }
      if (result == null) {
        result = message;
      } else if (!result.contains(message)) {
        result = result + separator + message;
      }
      separator = ": ";
      suppressed.addAll(Arrays.asList(t.getSuppressed()));
    }
    for (Throwable s : suppressed) {
      result = appendMessages(result, s, "; also: ");
    }
    return result;
  }
}
