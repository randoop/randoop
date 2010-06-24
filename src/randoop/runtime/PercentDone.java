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

  public PercentDone(double percentDone) {
    if (percentDone < 0 || percentDone > 100) {
      throw new IllegalArgumentException("percentDone outside range [0,100]");
    }
    this.percentDone = percentDone;
  }

  /**
   * Returns how close Randoop is to being done, as a percentage (between 0 and
   * 100).
   */
  public double getPercentDone() {
    return percentDone;
  }
}
