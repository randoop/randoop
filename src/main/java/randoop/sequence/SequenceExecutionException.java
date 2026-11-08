package randoop.sequence;

/** Represents an error that occurs during execution of a sequence. */
public class SequenceExecutionException extends RuntimeException {
  /** ID for serializing this class. */
  private static final long serialVersionUID = -5962830721815152881L;

  /**
   * Creates a SequenceExecutionException with the given message and cause.
   *
   * @param s the error message
   * @param e the exception that caused this error
   */
  public SequenceExecutionException(String s, Throwable e) {
    super(s, e);
  }
}
