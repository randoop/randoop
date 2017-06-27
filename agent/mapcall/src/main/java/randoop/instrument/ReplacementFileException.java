package randoop.instrument;

/** Exception representing an error in a replacement file. */
class ReplacementFileException extends Throwable {

  /** The line number where the error occurred */
  private final int lineNumber;

  /** The text of the line where the error occurred */
  private final String lineText;

  /**
   * Creates a {@link ReplacementFileException} with the message.
   *
   * @param msg the exception message
   */
  ReplacementFileException(String msg, int lineNumber, String lineText) {
    super(msg);
    this.lineNumber = lineNumber;
    this.lineText = lineText;
  }

  /**
   * The line number for the line where the error occurs.
   *
   * @return the line number for the line where the error occurs
   */
  int getLineNumber() {
    return lineNumber;
  }

  /**
   * Return the text for the line where the error occurs.
   *
   * @return the text for the line where the error occurs
   */
  String getLineText() {
    return lineText;
  }

  public String toString() {
    return String.format("%s on line %d: %s", getMessage(), getLineNumber(), getLineText());
  }
}
