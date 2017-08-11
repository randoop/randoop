package randoop.instrument;

/** Exception representing an error in a replacement file. */
class ReplacementFileException extends Throwable {

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
  ReplacementFileException(String msg, String filename, int lineNumber, String lineText) {
    super(msg);
    this.filename = filename;
    this.lineNumber = lineNumber;
    this.lineText = lineText;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Returns the message followed by the line text
   */
  @Override
  public String getMessage() {
    return String.format(
        "%s:%d: %s for line: %s", filename, lineNumber, super.getMessage(), lineText);
  }

  /**
   * {@inheritDoc}
   *
   * <p>Returns a string in the format {@code file-name:line-number: message}.
   */
  public String toString() {
    return String.format(
        "%s:%d: %s for line: %s", filename, lineNumber, super.toString(), lineText);
  }
}
