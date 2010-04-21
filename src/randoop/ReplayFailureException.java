package randoop;


/**
 * Thrown by a ContractFailureReplayVisitor or a RegressionReplayVisitor when a
 * regression decoration fails to replay.
 */
public class ReplayFailureException extends RuntimeException {

  private static final long serialVersionUID = -6685935677958691837L;
  private final Observation decoration;

  public ReplayFailureException(String message, Observation d) {
    super(message);
    this.decoration= d;
  }

  public Observation getDecoration() {
    return this.decoration;
  }
}
