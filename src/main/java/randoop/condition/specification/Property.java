package randoop.condition.specification;

/**
 * The representation of a boolean expression over the the values of parameters, receiver object,
 * and return value of a method. The identifiers refer to the values after the method is called. The
 * value of expressions prior to the method call can be referred to by wrapping the expression in
 * "{@code \old(-)}". For instance, an expression that states that the method call increases the
 * length of a {@code List} object named {@code list} by one would be written "{@code list.size() +
 * 1 == \old(list.size())}".
 *
 * <p>The JSON serialization of this class is used to read the specifications for an operation given
 * using the {@code --specifications} command-line option. The JSON should include a JSON object
 * labeled by the name of each field of this class, as in
 *
 * <pre>
 *   {
 *      "conditionText": {@code "result >= 0"},
 *      "description": "received value is non-negative"
 *   }
 * </pre>
 *
 * <p>The identifiers in the guard should be given in the {@link Identifiers} for the {@link
 * OperationSpecification} containing the {@code SpecificationClause} where the property occurs.
 *
 * <p>This is identical to {@link Guard}, but has a different name to distinguish them in the JSON
 * file.
 */
public class Property extends AbstractBooleanExpression {

  /**
   * Creates a {@link Property} with the given description and condition code.
   *
   * @param description the description of this boolean condition
   * @param conditionText the text of the Java code for the created condition
   */
  public Property(String description, String conditionText) {
    super(description, conditionText);
  }
}
