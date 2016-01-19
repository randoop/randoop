package randoop.sequence;

public class SequenceExceptionError extends Error {

  /** ID for serialization */
  private static final long serialVersionUID = 4778297090156993454L;
  private Throwable e;
  private ExecutableSequence sequence;
  private int pos;
  
  public SequenceExceptionError(ExecutableSequence sequence, int pos, Throwable e) {
    super("Exception thrown before end of sequence",e);
    this.sequence = sequence;
    this.pos = pos;
    this.e = e;
  }
  
  public Throwable getError() {
    return e;
  }

  public String getStatement() {
    return sequence.statementToCodeString(pos);
  }
  
  public String getSequence() {
    return sequence.toCodeString();
  }
}
