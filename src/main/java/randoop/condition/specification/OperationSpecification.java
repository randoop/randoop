package randoop.condition.specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A specification of a constructor or method, aka, an operation. Consists of the {@code
 * java.lang.reflect.AccessibleObject} for the operation, and lists of {@link ThrowsSpecification},
 * {@link ReturnSpecification}, and {@link ParamSpecification} objects that describe contracts on
 * the operation.
 *
 * <p>The JSON serialization of this class is used to read the specifications for an operation given
 * using the {@link randoop.main.GenInputsAbstract#specifications} command-line option. The JSON
 * should include a JSON object labeled by the name of each field of this class, as in
 *
 * <pre>
 *   {
 *     "operation": {
 *       "classname": "net.Connection",
 *       "name": "send",
 *       "parameterTypes": [
 *         "int"
 *       ]
 *     },
 *    "identifiers": {
 *       "parameters": [
 *         "code"
 *        ],
 *       "receiverName": "receiver",
 *       "returnName": "result"
 *     },
 *    "throwsSpecifications": [],
 *    "returnSpecifications": [],
 *    "paramSpecifications": [
 *      {
 *        "description": "the code must be positive",
 *        "guard": {
 *          "conditionText": {@code "code > 0"},
 *          "description": "the code must be positive"
 *         }
 *      }
 *    ]
 *   }
 * </pre>
 *
 * See the classes {@link Operation}, {@link Identifiers}, {@link ParamSpecification}, {@link
 * ReturnSpecification}, and {@link ThrowsSpecification} for details on specifying those objects.
 */
public class OperationSpecification {

  /** The reflection object for the operation */
  private final Operation operation;

  /** The identifier names used in the specifications */
  private final Identifiers identifiers;

  /** The specification of expected exceptions for the operation */
  private final List<ThrowsSpecification> throwsSpecifications;

  /** The list of post-conditions on the return value of the operation */
  private final List<ReturnSpecification> returnSpecifications;

  /** The list of pre-conditions on the parameters of the operation */
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
   * @param identifiers the {@link Identifiers} object, must be non-null
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
        && this.identifiers.equals(other.identifiers)
        && this.throwsSpecifications.equals(other.throwsSpecifications)
        && this.returnSpecifications.equals(other.returnSpecifications)
        && this.paramSpecifications.equals(other.paramSpecifications);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        this.operation,
        this.identifiers,
        this.throwsSpecifications,
        this.returnSpecifications,
        this.paramSpecifications);
  }

  @Override
  public String toString() {
    return "{ operation: "
        + this.operation.toString()
        + ", "
        + "identifiers: "
        + this.identifiers
        + ", "
        + "throwsSpecifications: "
        + this.throwsSpecifications
        + ", "
        + "returnSpecifications: "
        + this.returnSpecifications
        + ", "
        + "paramSpecifications: "
        + this.paramSpecifications
        + " }";
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
