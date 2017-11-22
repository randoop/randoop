package randoop.condition.specification;

/**
 * A {@link SpecificationClause} for pre-conditions on the parameters and receiver of an operation.
 * The pre-condition is expressed as a {@link Guard} that is to be checked before the operation is
 * invoked. If the guard evaluates to false on the arguments to the invocation, the operation should
 * not be invoked on the arguments. This means that the sequence with the particular call should be
 * classified as invalid, and discarded.
 *
 * <p>The JSON serialization of this class is used to read the specifications for an operation given
 * using the {@code --specifications} command-line option. The JSON should include a JSON object
 * labeled by the name of each field of this class, as in
 *
 * <pre>
 *   {
 *     "description": "the signalValue must be positive",
 *     "guard": {
 *        "conditionText": {@code "signalValue > 0"},
 *        "description": "the signalValue must be positive"
 *      }
 *   }
 * </pre>
 *
 * See {@link Guard} for details on specifying guards.
 */
public class Precondition extends SpecificationClause {

  /**
   * Create a {@link Precondition} with the given {@link Guard}.
   *
   * @param description the text description of the param-specification
   * @param guard the guard for the param-specification
   */
  public Precondition(String description, Guard guard) {
    super(description, guard);
  }

  @Override
  public String toString() {
    return "{ \"description\": \"" + getDescription() + "\", \"guard\": \"" + getGuard() + "\" }";
  }
}
