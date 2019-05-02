package randoop.sequence;

/**
 * Exception representing occurrence of a "flaky" test sequence where an exception was thrown by a
 * statement other than the last of the sequence. Occurs when a statement in an input sequence, that
 * previously terminated normally, throws an exception. Includes information needed to report the
 * error.
 */
public class SequenceExceptionError extends Error {

  /** ID for serialization. */
  private static final long serialVersionUID = 4778297090156993454L;

  /** The exception thrown by the sequence. */
  private Throwable e;

  /** The test sequence. */
  private ExecutableSequence testSequence;

  /** The position of the statement that threw the exception. */
  private int position;

  /**
   * Create an exception for the exception thrown by the statement at the given position in the test
   * sequence.
   *
   * @param testSequence the test sequence
   * @param position the position of the statement that threw the exception
   * @param exception the exception
   */
  public SequenceExceptionError(
      ExecutableSequence testSequence, int position, Throwable exception) {
    super("Exception thrown before end of sequence", exception);
    this.testSequence = testSequence;
    this.position = position;
    this.e = exception;
  }

  /**
   * Returns the thrown exception.
   *
   * @return the exception thrown by statement in sequence
   */
  public Throwable getError() {
    return e;
  }

  /**
   * Returns the string representation of the statement that threw the exception.
   *
   * @return the string representation of the statement
   */
  public String getStatement() {
    return testSequence.statementToCodeString(position);
  }

  /**
   * Returns the string representation of the test sequence.
   *
   * @return the full test sequence as a string
   */
  public String getSequence() {
    return testSequence.toCodeString();
  }

  /**
   * Returns the input sequence containing the statement that threw the exception.
   *
   * @return the input sequence from which exception was thrown
   */
  public Sequence getSubsequence() {
    return testSequence.sequence.getSubsequence(position);
  }
}
