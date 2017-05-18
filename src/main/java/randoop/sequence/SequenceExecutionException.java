package randoop.sequence;

/** Represents an error that occurs during execution of a sequence. */
public class SequenceExecutionException extends RuntimeException {
  private static final long serialVersionUID = -5962830721815152881L;

  public SequenceExecutionException(String s, Throwable e) {
    super(s, e);
  }
}
