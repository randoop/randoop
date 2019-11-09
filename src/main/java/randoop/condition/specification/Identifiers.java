package randoop.condition.specification;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.plumelib.util.UtilPlume;

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
 *     "receiverName": "receiver",
 *     "parameters": [
 *       "signalValue"
 *      ],
 *     "returnName": "result"
 *   }
 * </pre>
 *
 * <p>Names for the receiver and return value are optional; they default to "receiver" and "result".
 */
public class Identifiers {

  // NOTE: changing field names or @SerializedName annotations could affect integration with other
  // tools

  /** The receiver name. */
  private final String receiverName;

  /** The formal parameter names (not including the receiver). */
  private final List<String> parameters;

  /** The return value identifier. */
  private final String returnName;

  /**
   * Create an {@link Identifiers} object with the given names.
   *
   * @param receiverName the receiver name
   * @param parameters the list of identifiers for the operation formal parameters
   * @param returnName the return name
   */
  public Identifiers(String receiverName, List<String> parameters, String returnName) {
    this.receiverName = receiverName;
    this.parameters = parameters;
    this.returnName = returnName;
  }

  /**
   * Create an {@link Identifiers} object with the given parameter names and the default identifiers
   * for the receiver and return value.
   *
   * @param parameters the list of identifiers for the operation parameters
   */
  public Identifiers(List<String> parameters) {
    this("receiver", parameters, "result");
  }

  /**
   * Create a {@link Identifiers} object with no parameters and the default identifiers for the
   * receiver and return value.
   */
  public Identifiers() {
    this(Collections.emptyList());
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
   * Returns the parameter names in this {@link Identifiers} object.
   *
   * @return the parameter names
   */
  public List<String> getParameterNames() {
    return parameters;
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
   * Returns an identifier name that occurs more than once in this {@link Identifiers}, or null if
   * there are no duplicate names.
   *
   * @return a name occurs more than once, or null if there are no duplicate names
   */
  public String duplicateName() {
    Set<String> names = new HashSet<>();
    for (String name : parameters) {
      if (!names.add(name)) {
        return name;
      }
    }
    if (!names.add(receiverName)) {
      return receiverName;
    }
    if (!names.add(returnName)) {
      return returnName;
    }
    return null;
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }
    if (!(object instanceof Identifiers)) {
      return false;
    }
    Identifiers other = (Identifiers) object;
    return this.receiverName.equals(other.receiverName)
        && this.parameters.equals(other.parameters)
        && this.returnName.equals(other.returnName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.receiverName, this.parameters, this.returnName);
  }

  @Override
  public String toString() {
    return "{ \"receiverName\": "
        + "\""
        + receiverName
        + "\""
        + ", \"parameters\": "
        + "[ \""
        + UtilPlume.join(parameters, "\", \"")
        + "\"]"
        + ", \"returnName\": "
        + "\""
        + returnName
        + "\""
        + " }";
  }
}
