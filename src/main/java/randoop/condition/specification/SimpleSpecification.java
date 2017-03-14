package randoop.condition.specification;

/**
 * Represents the specification of either a condition (a boolean expression) or a throws assertion.
 */
public abstract class SimpleSpecification extends Specification {

  SimpleSpecification(String description) {
    super(description);
  }
}
