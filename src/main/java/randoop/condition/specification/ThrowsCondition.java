package randoop.condition.specification;

import com.google.gson.annotations.SerializedName;
import java.util.Objects;
import org.checkerframework.checker.signature.qual.ClassGetName;

/**
 * A specification clause that an exception should be thrown.
 *
 * <p>The JSON serialization of this class is used to read the specifications for an operation given
 * using the {@code --specifications} command-line option. The JSON should include a JSON object
 * labeled by the name of each field of this class, as in
 *
 * <pre>
 *   {
 *      "exception": "java.lang.IllegalStateException",
 *      "description": "throws IllegalStateException if the connection is already open",
 *      "guard": {
 *         "condition": "receiver.isOpen()",
 *         "description": "if the connection is already open"
 *       }
 *   }
 * </pre>
 *
 * See {@link Guard} for details on specifying guards.
 */
public class ThrowsCondition extends SpecificationClause {

  // NOTE: changing field names or @SerializedName annotations could affect integration with other
  // tools

  /** The fully-qualified name of the type of the expected exception. */
  @SerializedName("exception")
  private final @ClassGetName String exceptionType;

  /** Gson serialization requires a default constructor. */
  @SuppressWarnings({
    "unused",
    "signature" // dummy value
  })
  private ThrowsCondition() {
    super();
    this.exceptionType = "";
  }

  /**
   * Creates a {@link ThrowsCondition} representing an exception expected when the guard is true.
   *
   * @param description the description of the condition
   * @param guard the guard for the specification
   * @param exceptionType the expected exception type
   */
  public ThrowsCondition(String description, Guard guard, @ClassGetName String exceptionType) {
    super(description, guard);
    this.exceptionType = exceptionType;
  }

  /**
   * Returns the exception type name for this {@link ThrowsCondition}.
   *
   * @return the exception type name for this throws specification
   */
  public @ClassGetName String getExceptionTypeName() {
    return exceptionType;
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof ThrowsCondition)) {
      return false;
    }
    ThrowsCondition other = (ThrowsCondition) object;
    return super.equals(other) && this.exceptionType.equals(other.exceptionType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), exceptionType);
  }

  @Override
  public String toString() {
    return "{ \"description\": \""
        + getDescription()
        + "\", \"guard\": \""
        + getGuard()
        + ", \"exceptionType\": "
        + exceptionType
        + "\" }";
  }
}
