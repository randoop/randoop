package randoop.condition.specification;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import plume.UtilMDE;

/**
 * Contains the identifiers used in the guards and properties of the specifications in a {@link
 * OperationSpecification}. The order of the names is significant, but the names need not match the
 * actual declaration of the method.
 *
 * <p>The JSON serialization of this class is used to read the specifications for an operation given
 * using the {@code --specifications} command-line option. The JSON should include a JSON object
 * labeled by the name of each field of this class, as in
 *
 * <pre>
 *   {
 *     "parameters": [
 *       "signalValue"
 *      ],
 *     "receiverName": "receiver",
 *     "returnName": "result"
 *   }
 * </pre>
 *
 * <p>When using the class, if names are not given for the receiver and return value, then the
 * defaults {@link #DEFAULT_RECEIVER_NAME} and {@link #DEFAULT_RETURN_NAME} will be used.
 */
public class Identifiers {

  // NOTE: changing field names or @SerializedName annotations could affect integration with other tools

  /** The default identifier name for receiver (value: "receiver") */
  private static final String DEFAULT_RECEIVER_NAME = "receiver";

  /** The default identifier name for the return value (value: "result") */
  private static final String DEFAULT_RETURN_NAME = "result";

  /** The formal parameter names (not including the receiver name) */
  private final List<String> parameters;

  /** The receiver name. */
  private final String receiverName;

  /** The return value identifier. */
  private final String returnName;

  /**
   * Create an {@link Identifiers} object with the given names.
   *
   * @param parameters the list of identifiers for the operation formal parameters
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

  @Override
  public String toString() {
    return "{ \"parameters\": "
        + "[ \""
        + UtilMDE.join(parameters, "\", \"")
        + "\"]"
        + ", \"receiverName\": "
        + "\""
        + receiverName
        + "\""
        + ", \"returnName\": "
        + "\""
        + returnName
        + "\""
        + " }";
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

  /**
   * Indicates whether any identifier names occur more than once in this {@link Identifiers}.
   *
   * @return true if a name occurs more than once, false otherwise
   */
  public boolean hasDuplicatedName() {
    Set<String> names = new HashSet<>(parameters);
    return names.size() != parameters.size()
        || parameters.contains(receiverName)
        || parameters.contains(returnName)
        || receiverName.equals(returnName);
  }
}
