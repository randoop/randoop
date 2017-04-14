package randoop.condition.specification;

import com.google.gson.annotations.SerializedName;
import java.util.Objects;

/**
 * The specification for a {@link ThrowsSpecification} that specifies that an exception should be
 * thrown.
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
public class ThrowsSpecification extends Specification {

  /** The fully-qualified name of the type of the expected exception */
  @SerializedName("exception")
  private final String exceptionType;

  private ThrowsSpecification() {
    super();
    this.exceptionType = "";
  }

  /**
   * Creates a {@link ThrowsSpecification} representing an exception expected when the guard is
   * true.
   *
   * @param description the description of the condition
   * @param guard the guard for the specification
   * @param exceptionType the expected exception type
   */
  public ThrowsSpecification(String description, Guard guard, String exceptionType) {
    super(description, guard);
    this.exceptionType = exceptionType;
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof ThrowsSpecification)) {
      return false;
    }
    ThrowsSpecification other = (ThrowsSpecification) object;
    return super.equals(other) && this.exceptionType.equals(other.exceptionType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), exceptionType);
  }

  @Override
  public String toString() {
    return "{ guard: " + getGuard() + ", exceptionType: " + exceptionType + " }";
  }

  /**
   * Returns the exception type name for this {@link ThrowsSpecification}.
   *
   * @return the exception type name for this throws specification
   */
  public String getExceptionTypeName() {
    return exceptionType;
  }
}
