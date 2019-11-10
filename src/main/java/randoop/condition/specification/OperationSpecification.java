package randoop.condition.specification;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A specification of a constructor or method, aka, an <i>operation</i>. Consists of the {@code
 * java.lang.reflect.AccessibleObject} for the operation, and lists of {@link Precondition}, {@link
 * Postcondition}, and {@link ThrowsCondition} objects that describe contracts on the operation.
 *
 * <p>Method {@link randoop.condition.SpecificationCollection#create(java.util.List)} reads
 * specifications from JSON files. A user specifies JSON files using the {@code --specifications}
 * command-line option. The JSON should include a JSON object labeled by the name of each field of
 * this class, as in
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
 *         "signalValue"
 *        ],
 *       "receiverName": "receiver",
 *       "returnName": "result"
 *     },
 *    "preSpecifications": [
 *      {
 *        "description": "the signalValue must be positive",
 *        "guard": {
 *          "conditionText": {@code "signalValue > 0"},
 *          "description": "the signalValue must be positive"
 *         }
 *      }
 *    "postSpecifications": [],
 *    "throwsSpecifications": [],
 *    ]
 *   }
 * </pre>
 *
 * Method {@link
 * randoop.condition.SpecificationCollection#getExecutableSpecification(java.lang.reflect.Executable)}
 * translates specifications to an {@link randoop.condition.ExecutableSpecification} object that
 * allows the underlying Boolean expressions to be evaluated.
 */
public class OperationSpecification {

  // NOTE: changing field names or @SerializedName annotations could affect integration with other
  // tools

  /** The reflection object for the operation. */
  private final OperationSignature operation;

  /** The identifier names used in the specifications. */
  private final Identifiers identifiers;

  /** The list of pre-conditions for the operation. */
  @SerializedName("pre")
  private final List<Precondition> preSpecifications;

  /** The list of post-conditions for the operation. */
  @SerializedName("post")
  private final List<Postcondition> postSpecifications;

  /** The specification of expected exceptions for the operation. */
  @SerializedName("throws")
  private final List<ThrowsCondition> throwsSpecifications;

  /** Gson serialization requires a default constructor. */
  @SuppressWarnings("unused")
  private OperationSpecification() {
    this.operation = null;
    this.identifiers = new Identifiers();
    this.preSpecifications = new ArrayList<>();
    this.postSpecifications = new ArrayList<>();
    this.throwsSpecifications = new ArrayList<>();
  }

  /**
   * Creates an {@link OperationSpecification} for the given operation with no specifications.
   *
   * @param operation the {@link OperationSignature} object, must be non-null
   * @param identifiers the {@link Identifiers} object, must be non-null
   */
  public OperationSpecification(OperationSignature operation, Identifiers identifiers) {
    this(
        operation,
        identifiers,
        new ArrayList<Precondition>(),
        new ArrayList<Postcondition>(),
        new ArrayList<ThrowsCondition>());
  }

  /**
   * Creates an {@link OperationSpecification} for the given operation with the given
   * specifications.
   *
   * @param operation the reflection object for the operation, must be non-null
   * @param identifiers the identifiers used in the specifications
   * @param preSpecifications the list of param specifications for the operation
   * @param postSpecifications the list of return specifications for the operation
   * @param throwsSpecifications the list of specifications for the operation
   */
  public OperationSpecification(
      OperationSignature operation,
      Identifiers identifiers,
      List<Precondition> preSpecifications,
      List<Postcondition> postSpecifications,
      List<ThrowsCondition> throwsSpecifications) {
    this.operation = operation;
    this.identifiers = identifiers;
    this.preSpecifications = preSpecifications;
    this.postSpecifications = postSpecifications;
    this.throwsSpecifications = throwsSpecifications;
  }

  /**
   * Return the {@link OperationSignature} for the operation.
   *
   * @return the reflection object for the operation
   */
  public OperationSignature getOperation() {
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
   * Return the list of {@link Precondition} objects for this {@link OperationSpecification}.
   *
   * @return the list of {@link Precondition} objects for this specification
   */
  public List<Precondition> getPreconditions() {
    return preSpecifications;
  }

  /**
   * Return the list of {@link Postcondition} objects for this {@link OperationSpecification}.
   *
   * @return the list of {@link Postcondition} objects for this specification
   */
  public List<Postcondition> getPostconditions() {
    return postSpecifications;
  }

  /**
   * Return the list of {@link ThrowsCondition} objects for this {@link OperationSpecification}.
   *
   * @return the list of specifications for this operation specification, is non-null
   */
  public List<ThrowsCondition> getThrowsConditions() {
    return throwsSpecifications;
  }

  /**
   * Adds {@link Precondition} objects from the list to this {@link OperationSpecification}.
   *
   * @param specifications the list of {@link Precondition} objects
   */
  public void addParamSpecifications(List<Precondition> specifications) {
    preSpecifications.addAll(specifications);
  }

  /**
   * Adds {@link Postcondition} objects from the list to this {@link OperationSpecification}.
   *
   * @param specifications the list of {@link Postcondition} objects
   */
  public void addReturnSpecifications(List<Postcondition> specifications) {
    postSpecifications.addAll(specifications);
  }

  /**
   * Adds {@link ThrowsCondition} objects from the list to this {@link OperationSpecification}.
   *
   * @param specifications the list of {@link ThrowsCondition} objects
   */
  public void addThrowsConditions(List<ThrowsCondition> specifications) {
    throwsSpecifications.addAll(specifications);
  }

  /**
   * Indicates whether this {@link OperationSpecification} contains any pre-, post-, or
   * throws-specifications.
   *
   * @return {@code true} if there are no pre-, post-, or throws-specifications, false otherwise
   */
  public boolean isEmpty() {
    return preSpecifications.isEmpty()
        && postSpecifications.isEmpty()
        && throwsSpecifications.isEmpty();
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }
    if (!(object instanceof OperationSpecification)) {
      return false;
    }
    OperationSpecification other = (OperationSpecification) object;
    return this.operation.equals(other.operation)
        && this.identifiers.equals(other.identifiers)
        && this.preSpecifications.equals(other.preSpecifications)
        && this.postSpecifications.equals(other.postSpecifications)
        && this.throwsSpecifications.equals(other.throwsSpecifications);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        this.operation,
        this.identifiers,
        this.preSpecifications,
        this.postSpecifications,
        this.throwsSpecifications);
  }

  @Override
  public String toString() {
    return "{ \"operation\": "
        + this.operation.toString()
        + ", "
        + "\"identifiers\": "
        + this.identifiers
        + ", "
        + "\"preSpecifications\": "
        + this.preSpecifications
        + " }"
        + ", "
        + "\"postSpecifications\": "
        + this.postSpecifications
        + ", "
        + "\"throwsSpecifications\": "
        + this.throwsSpecifications;
  }
}
