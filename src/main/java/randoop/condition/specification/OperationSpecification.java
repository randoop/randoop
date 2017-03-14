package randoop.condition.specification;

import java.lang.reflect.AccessibleObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A specification of a constructor or method, aka, an operation. Consists of the {@code
 * java.lang.reflect.AccessibleObject} for the operation, and a list of {@link Specification}
 * objects that describe contracts on the operation.
 */
public class OperationSpecification {

  /** the reflection object for the operation */
  private final AccessibleObject operation;

  /** the specifications for the operation */
  private final List<Specification> specifications;

  /**
   * Creates an {@link OperationSpecification} for the given operation with no specifications.
   *
   * @param operation the reflection object for the operation, must be non-null
   */
  public OperationSpecification(AccessibleObject operation) {
    this.operation = operation;
    this.specifications = new ArrayList<>();
  }

  /**
   * Creates an {@link OperationSpecification} for the given operation with the given
   * specifications.
   *
   * @param operation the reflection object for the operation, must be non-null
   * @param specifications the list of specifications for the object
   */
  public OperationSpecification(AccessibleObject operation, List<Specification> specifications) {
    this.operation = operation;
    this.specifications = specifications;
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof OperationSpecification)) {
      return false;
    }
    OperationSpecification other = (OperationSpecification) object;
    return this.specifications.equals(other.specifications);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.specifications);
  }

  @Override
  public String toString() {
    return specifications.toString();
  }

  /**
   * Adds a {@link Specification} to this {@link OperationSpecification}.
   *
   * @param specification the specification to add to this operation specification
   */
  public void addSpecification(Specification specification) {
    specifications.add(specification);
  }

  /**
   * Return the {@code java.lang.reflect.AccessibleObject} for the operation
   *
   * @return the reflection object for the operation
   */
  public AccessibleObject getOperation() {
    return operation;
  }

  /**
   * Return the list of {@link Specification} objects for this operation specification.
   *
   * @return the list of specifications for this operation specification, is non-null
   */
  public List<Specification> getSpecifications() {
    return specifications;
  }
}
