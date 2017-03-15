package randoop.condition.specification;

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
  private final Operation operation;

  /** the specifications for the operation */
  private final List<ThrowsSpecification> throwsSpecifications;

  private final List<ReturnSpecification> returnSpecifications;
  private final List<ParamSpecification> paramSpecifications;

  /**
   * Creates an {@link OperationSpecification} for the given operation with no specifications.
   *
   * @param operation the reflection object for the operation, must be non-null
   */
  public OperationSpecification(Operation operation) {
    this.operation = operation;
    this.throwsSpecifications = new ArrayList<>();
    this.returnSpecifications = new ArrayList<>();
    this.paramSpecifications = new ArrayList<>();
  }

  /**
   * Creates an {@link OperationSpecification} for the given operation with the given
   * specifications.
   *
   * @param operation the reflection object for the operation, must be non-null
   * @param throwsSpecifications the list of specifications for the operation
   * @param returnSpecifications the list of return specifications for the operation
   * @param paramSpecifications the list of param specifications for the operation
   */
  public OperationSpecification(
      Operation operation,
      List<ThrowsSpecification> throwsSpecifications,
      List<ReturnSpecification> returnSpecifications,
      List<ParamSpecification> paramSpecifications) {
    this.operation = operation;
    this.throwsSpecifications = throwsSpecifications;
    this.returnSpecifications = returnSpecifications;
    this.paramSpecifications = paramSpecifications;
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof OperationSpecification)) {
      return false;
    }
    OperationSpecification other = (OperationSpecification) object;
    return this.operation.equals(other.operation)
        && this.throwsSpecifications.equals(other.throwsSpecifications)
        && this.returnSpecifications.equals(other.returnSpecifications)
        && this.paramSpecifications.equals(other.paramSpecifications);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        this.operation,
        this.throwsSpecifications,
        this.returnSpecifications,
        this.paramSpecifications);
  }

  @Override
  public String toString() {
    return this.operation.toString();
  }

  public void addThrowsSpecifications(List<ThrowsSpecification> specifications) {
    throwsSpecifications.addAll(specifications);
  }

  public void addReturnSpecifications(List<ReturnSpecification> specifications) {
    returnSpecifications.addAll(specifications);
  }

  public void addParamSpecifications(List<ParamSpecification> specifications) {
    paramSpecifications.addAll(specifications);
  }

  /**
   * Return the {@code java.lang.reflect.AccessibleObject} for the operation
   *
   * @return the reflection object for the operation
   */
  public Operation getOperation() {
    return operation;
  }

  /**
   * Return the list of {@link Specification} objects for this operation specification.
   *
   * @return the list of specifications for this operation specification, is non-null
   */
  public List<ThrowsSpecification> getThrowsSpecifications() {
    return throwsSpecifications;
  }

  public List<ReturnSpecification> getReturnSpecifications() {
    return returnSpecifications;
  }

  public List<ParamSpecification> getParamSpecifications() {
    return paramSpecifications;
  }
}
