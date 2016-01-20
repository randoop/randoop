package randoop.sequence;

/**
 * Exception representing occurrence of a "flaky" test sequence where an
 * exception was thrown by a statement other than the last of the sequence.
 * 
 */
public class SequenceExceptionError extends Error {

  /** ID for serialization */
  private static final long serialVersionUID = 4778297090156993454L;
  
  /** The exception thrown by the sequence */
  private Throwable e;
  
  /** The test sequence */
  private ExecutableSequence sequence;
  
  /** The position of the statement that threw the exception */
  private int position;
  
  /**
   * Create an exception for the exception thrown by the statement at the given
   * position in the test sequence.
   * 
   * @param sequence  the test sequence
   * @param position  the position of the statement that threw the exception
   * @param exception  the exception
   */
  public SequenceExceptionError(ExecutableSequence sequence, 
                                int position, 
                                Throwable exception) {
    super("Exception thrown before end of sequence", exception);
    this.sequence = sequence;
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
    return sequence.statementToCodeString(position);
  }
  
  /**
   * Returns the string representation of the test sequence.
   * 
   * @return the full test sequence as a string
   */
  public String getSequence() {
    return sequence.toCodeString();
  }
}
