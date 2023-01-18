package randoop;

import randoop.test.Check;

/**
 * Thrown by a ContractFailureReplayVisitor or a RegressionReplayVisitor when a regression
 * decoration fails to replay.
 */
public class ReplayFailureException extends RuntimeException {

  private static final long serialVersionUID = -6685935677958691837L;

  /** The check that failed. */
  @SuppressWarnings("serial")
  private final Check decoration;

  public ReplayFailureException(String message, Check d) {
    super(message);
    this.decoration = d;
  }

  public Check getDecoration() {
    return this.decoration;
  }
}
