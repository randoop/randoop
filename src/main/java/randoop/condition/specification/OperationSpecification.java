package randoop.condition.specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A specification of a constructor or method, aka, an operation. Consists of the {@code
 * java.lang.reflect.AccessibleObject} for the operation, and lists of {@link ThrowsSpecification},
 * {@link ReturnSpecification}, and {@link ParamSpecification} objects that describe contracts on
 * the operation.
 */
public class OperationSpecification {

  /** the reflection object for the operation */
  private final Operation operation;

  /** the identifier names used in the specifications */
  private final Identifiers identifiers;

  /** the specification of expected exceptions for the operation */
  private final List<ThrowsSpecification> throwsSpecifications;

  /** the list of post-conditions on the return value of the operation */
  private final List<ReturnSpecification> returnSpecifications;

  /** the list of pre-conditions on the parameters of the operation */
  private final List<ParamSpecification> paramSpecifications;

  /**
   * Creates an {@link OperationSpecification} for the given operation with no specifications and
   * the default receiver and return value identifiers.
   *
   * @param operation the {@link Operation} object, must be non-null
   */
  public OperationSpecification(Operation operation) {
    this(operation, new Identifiers());
  }

  /**
   * Creates an {@link OperationSpecification} for the given operation with no specifications.
   *
   * @param operation the {@link Operation} object, must be non-null
   */
  public OperationSpecification(Operation operation, Identifiers identifiers) {
    this(
        operation,
        identifiers,
        new ArrayList<ThrowsSpecification>(),
        new ArrayList<ReturnSpecification>(),
        new ArrayList<ParamSpecification>());
  }

  /**
   * Creates an {@link OperationSpecification} for the given operation with the given
   * specifications.
   *
   * @param operation the reflection object for the operation, must be non-null
   * @param identifiers the identifiers used in the specifications
   * @param throwsSpecifications the list of specifications for the operation
   * @param returnSpecifications the list of return specifications for the operation
   * @param paramSpecifications the list of param specifications for the operation
   */
  public OperationSpecification(
      Operation operation,
      Identifiers identifiers,
      List<ThrowsSpecification> throwsSpecifications,
      List<ReturnSpecification> returnSpecifications,
      List<ParamSpecification> paramSpecifications) {
    this.operation = operation;
    this.identifiers = identifiers;
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
   * Return the {@link Identifiers} for this specification.
   *
   * @return the identifiers for this specification
   */
  public Identifiers getIdentifiers() {
    return identifiers;
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
