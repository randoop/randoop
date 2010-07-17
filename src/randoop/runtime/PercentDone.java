package randoop.runtime;

/**
 * A Randoop message that reports how close test generation is close
 * to being done.
 */
public class PercentDone implements IMessage {

  private static final long serialVersionUID = 4577012243475560892L;
  
  /**
   * Invariant: percentDone is between 0 and 100, inclusive.
   */
  private final double percentDone;

  private final int sequencesGenerated;

  private final int errorsRevealed;

  public PercentDone(double percentDone, int sequencesGenerated, int errorsRevealed) {
    if (percentDone < 0 || percentDone > 100) {
      throw new IllegalArgumentException("percentDone outside range [0,100]");
    }
    if (sequencesGenerated < 0) {
      throw new IllegalArgumentException("sequencesGenerated is negative");
    }
    if (errorsRevealed < 0) {
      throw new IllegalArgumentException("errorsRevealed is negative");
    }
    this.percentDone = percentDone;
    this.sequencesGenerated = sequencesGenerated;
    this.errorsRevealed = errorsRevealed;
  }

  /**
   * Returns how close Randoop is to being done, as a percentage (between 0 and
   * 100).
   */
  public double getPercentDone() {
    return percentDone;
  }

  public int getSequencesGenerated() {
    return sequencesGenerated;
  }

  public int getNumErrors() {
    return errorsRevealed;
  }
}
