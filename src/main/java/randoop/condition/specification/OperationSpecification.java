package randoop.condition.specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A specification of a constructor or method, aka, an operation. Consists of the {@code
 * java.lang.reflect.AccessibleObject} for the operation, and lists of {@link ThrowsSpecification},
 * {@link PostSpecification}, and {@link PreSpecification} objects that describe contracts on the
 * operation.
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
 *    "postSpecifications": [],
 *    "preSpecifications": [
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
 * See the classes {@link Operation}, {@link Identifiers}, {@link PreSpecification}, {@link
 * PostSpecification}, and {@link ThrowsSpecification} for details on specifying those objects.
 */
public class OperationSpecification {

  /** The reflection object for the operation */
  private final Operation operation;

  /** The identifier names used in the specifications */
  private final Identifiers identifiers;

  /** The specification of expected exceptions for the operation */
  private final List<ThrowsSpecification> throwsSpecifications;

  /** The list of post-conditions on the return value of the operation */
  private final List<PostSpecification> postSpecifications;

  /** The list of pre-conditions on the parameters of the operation */
  private final List<PreSpecification> preSpecifications;

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
        new ArrayList<PostSpecification>(),
        new ArrayList<PreSpecification>());
  }

  /**
   * Creates an {@link OperationSpecification} for the given operation with the given
   * specifications.
   *
   * @param operation the reflection object for the operation, must be non-null
   * @param identifiers the identifiers used in the specifications
   * @param throwsSpecifications the list of specifications for the operation
   * @param postSpecifications the list of return specifications for the operation
   * @param preSpecifications the list of param specifications for the operation
   */
  public OperationSpecification(
      Operation operation,
      Identifiers identifiers,
      List<ThrowsSpecification> throwsSpecifications,
      List<PostSpecification> postSpecifications,
      List<PreSpecification> preSpecifications) {
    this.operation = operation;
    this.identifiers = identifiers;
    this.throwsSpecifications = throwsSpecifications;
    this.postSpecifications = postSpecifications;
    this.preSpecifications = preSpecifications;
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
        && this.postSpecifications.equals(other.postSpecifications)
        && this.preSpecifications.equals(other.preSpecifications);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        this.operation,
        this.identifiers,
        this.throwsSpecifications,
        this.postSpecifications,
        this.preSpecifications);
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
        + "postSpecifications: "
        + this.postSpecifications
        + ", "
        + "preSpecifications: "
        + this.preSpecifications
        + " }";
  }

  /**
   * Adds {@link ThrowsSpecification} objects from the list to this {@link OperationSpecification}.
   *
   * @param specifications the list of {@link ThrowsSpecification} objects
   */
  public void addThrowsSpecifications(List<ThrowsSpecification> specifications) {
    throwsSpecifications.addAll(specifications);
  }

  /**
   * Adds {@link PostSpecification} objects from the list to this {@link OperationSpecification}.
   *
   * @param specifications the list of {@link PostSpecification} objects
   */
  public void addReturnSpecifications(List<PostSpecification> specifications) {
    postSpecifications.addAll(specifications);
  }

  /**
   * Adds {@link PreSpecification} objects from the list to this {@link OperationSpecification}.
   *
   * @param specifications the list of {@link PreSpecification} objects
   */
  public void addParamSpecifications(List<PreSpecification> specifications) {
    preSpecifications.addAll(specifications);
  }

  /**
   * Indicates whether this {@link OperationSpecification} contains any pre-, post- or
   * throws-specifications.
   *
   * @return {@code true} if there are no pre-, post- or throws-specifications, false otherwise
   */
  public boolean isEmpty() {
    return throwsSpecifications.isEmpty()
        && postSpecifications.isEmpty()
        && preSpecifications.isEmpty();
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
   * Return the list of {@link ThrowsSpecification} objects for this {@link OperationSpecification}.
   *
   * @return the list of specifications for this operation specification, is non-null
   */
  public List<ThrowsSpecification> getThrowsSpecifications() {
    return throwsSpecifications;
  }

  /**
   * Return the list of {@link PostSpecification} objects for this {@link OperationSpecification}.
   *
   * @return the list of {@link PostSpecification} objects for this specification
   */
  public List<PostSpecification> getPostSpecifications() {
    return postSpecifications;
  }

  /**
   * Return the list of {@link PreSpecification} objects for this {@link OperationSpecification}.
   *
   * @return the list of {@link PreSpecification} objects for this specification
   */
  public List<PreSpecification> getPreSpecifications() {
    return preSpecifications;
  }
}
