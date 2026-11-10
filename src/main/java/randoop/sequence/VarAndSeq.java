package randoop.sequence;

/**
 * A pair of a {@link Variable} and a {@link Sequence}. This is used to pass around the last
 * variable and the sequence it belongs to.
 */
public class VarAndSeq {
  /** The variable in the pair. */
  private final Variable var;

  /** The sequence in the pair. */
  private final Sequence seq;

  /**
   * Constructs a new {@link VarAndSeq} with the given variable and sequence.
   *
   * @param var the variable
   * @param seq the sequence
   */
  public VarAndSeq(Variable var, Sequence seq) {
    this.var = var;
    this.seq = seq;
  }

  /**
   * Returns the variable.
   *
   * @return the variable
   */
  public Variable getVariable() {
    return var;
  }

  /**
   * Returns the sequence.
   *
   * @return the sequence
   */
  public Sequence getSequence() {
    return seq;
  }
}
