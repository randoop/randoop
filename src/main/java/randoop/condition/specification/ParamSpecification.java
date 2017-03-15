package randoop.condition.specification;

/**
 * A {@link Specification} for pre-conditions on the parameters and receiver of an operation. The
 * pre-condition is expressed as a {@link Guard} that is to be checked before the operation is
 * invoked. If the guard evaluates to false on the arguments to the invocation, the operation should
 * not be invoked on the arguments. This means that the sequence with the particular call should be
 * classified as invalid, and discarded.
 */
public class ParamSpecification extends Specification {

  /**
   * Create a {@link ParamSpecification} with the given {@link Guard}.
   *
   * @param description the text description of the param-specification
   * @param guard the guard for the param-specification
   */
  public ParamSpecification(String description, Guard guard) {
    super(description, guard);
  }
}
