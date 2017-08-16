package randoop.condition.specification;

/**
 * The representation of a boolean expression over the values of parameters and receiver object of
 * an operation (i.e., a method or constructor). The identifiers refer to the values before the
 * operation is called.
 *
 * <p>The JSON serialization of this class is used to read the specifications for an operation given
 * using the {@code --specifications} command-line option. The JSON should include a JSON object
 * labeled by the name of each field of this class, as in
 *
 * <pre>
 *   {
 *      "conditionText": {@code "signalValue > 0"},
 *      "description": "the signal value must be positive"
 *   }
 * </pre>
 *
 * <p>where {@code signalValue} is a declared identifier in the specification.
 *
 * <p>The identifiers in the property should be given in the {@link Identifiers} for the {@link
 * OperationSpecification} containing the {@link Postcondition} where the property occurs.
 *
 * <p>This is identical to {@link Property}, but has a different name to distinguish them in the
 * JSON file.
 *
 * @see SpecificationClause
 */
public class Guard extends AbstractBooleanExpression {

  /**
   * Creates a {@link Guard} with the given description and condition source code.
   *
   * @param description the description of this boolean condition
   * @param conditionText the text of the Java code for the created condition
   */
  public Guard(String description, String conditionText) {
    super(description, conditionText);
  }
}
