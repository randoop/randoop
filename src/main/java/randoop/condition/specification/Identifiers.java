package randoop.condition.specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Contains the identifiers used in the guards and properties of the specifications in a {@link
 * OperationSpecification}.
 */
public class Identifiers {

  /** the default identifier name for receiver */
  private static final String DEFAULT_RECEIVER_NAME = "receiver";

  /** the default identifier name for the return value */
  private static final String DEFAULT_RETURN_NAME = "result";

  /** the parameter names for the operation */
  private final List<String> parameters;

  /** the receiver name for the specifications */
  private final String receiverName;

  /** the return value identifier for the specifications */
  private final String returnName;

  /**
   * Create an {@link Identifiers} object with the given names.
   *
   * @param parameters the list of identifiers for the operation parameters
   * @param receiverName the receiver name
   * @param returnName the return name
   */
  public Identifiers(List<String> parameters, String receiverName, String returnName) {
    this.parameters = parameters;
    this.receiverName = receiverName;
    this.returnName = returnName;
  }

  /**
   * Create an {@link Identifiers} object with the given parameter names and the default identifiers
   * for the receiver and return value.
   *
   * @param parameters the list of identifiers for the operation parameters
   */
  public Identifiers(List<String> parameters) {
    this(parameters, DEFAULT_RECEIVER_NAME, DEFAULT_RETURN_NAME);
  }

  /**
   * Create a {@link Identifiers} object with no parameters and the default identifiers for the
   * receiver and return value.
   */
  public Identifiers() {
    this(new ArrayList<String>());
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof Identifiers)) {
      return false;
    }
    Identifiers other = (Identifiers) object;
    return this.parameters.equals(other.parameters)
        && this.receiverName.equals(other.receiverName)
        && this.returnName.equals(other.returnName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.parameters, this.receiverName, this.returnName);
  }

  /**
   * Returns the parameter names in this {@link Identifiers} object.
   *
   * @return the parameter names
   */
  public List<String> getParameterNames() {
    return parameters;
  }

  /**
   * Returns the identifier for the receiver object in this {@link Identifiers} object.
   *
   * @return the receiver name
   */
  public String getReceiverName() {
    return receiverName;
  }

  /**
   * Returns the identifier for the return value in this {@link Identifiers} object.
   *
   * @return the return value identifier
   */
  public String getReturnName() {
    return returnName;
  }
}
