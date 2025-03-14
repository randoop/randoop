package randoop.contract;

import java.util.Arrays;
import java.util.Objects;
import org.plumelib.util.StringsPlume;
import randoop.sequence.StringTooLongException;
import randoop.sequence.Value;
import randoop.types.JavaTypes;
import randoop.types.Type;
import randoop.types.TypeTuple;

/**
 * A check recording the value of a primitive value (or String) obtained during execution (e.g.,
 * {@code var3 == 1} where {@code var3} is an integer-valued variable in a Randoop test).
 */
public final class PrimValue extends ObjectContract {

  /** Specifies what type of equality the contract uses. */
  public enum EqualityMode {
    /** Use reference equality {@code ==}. */
    EQUALSEQUALS,
    /** Use abstract equality {@code .equals()}. */
    EQUALSMETHOD
  }

  /**
   * The expected run-time value. It is a boxed primitive or String (checked during construction).
   */
  public final Object value;

  /** Whether to use {@code ==} or {@code .equals()} to test for equality. */
  private final EqualityMode equalityMode;

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof PrimValue)) {
      return false;
    }
    PrimValue other = (PrimValue) o;
    return value.equals(other.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }

  /**
   * @param value the value for the expression: a primitive value or string
   * @param equalityMode what equality test the assertion uses
   */
  public PrimValue(Object value, EqualityMode equalityMode) {
    if (value == null) {
      throw new IllegalArgumentException("value cannot be null");
    }
    Type type = Type.forClass(value.getClass());
    if (!type.isBoxedPrimitive() && !type.isString()) {
      throw new IllegalArgumentException(
          "value is not a primitive or string : " + value.getClass());
    }
    if (value instanceof String && !Value.escapedStringLengthOk((String) value)) {
      throw new StringTooLongException((String) value);
    }
    this.value = value;
    this.equalityMode = equalityMode;
  }

  @Override
  public boolean evaluate(Object... objects) throws Throwable {
    assert objects.length == 1;
    return value.equals(objects[0]);
  }

  @Override
  public int getArity() {
    return 1;
  }

  /** The arguments to which this contract can be applied. */
  static TypeTuple inputTypes = new TypeTuple(Arrays.asList(JavaTypes.OBJECT_TYPE));

  @Override
  public TypeTuple getInputTypes() {
    return inputTypes;
  }

  @Override
  public String toString() {
    return "randoop.PrimValue, value=" + StringsPlume.escapeJava(value.toString());
  }

  @Override
  public String get_observer_str() {
    return "PrimValue";
  }

  @Override
  public String toCodeString() {
    // ValueExpression represents the value of a variable.
    // We special-case printing for this type of expression,
    // to improve readability.
    if (value.equals(Double.NaN)) {
      return "org.junit.Assert.assertTrue(Double.isNaN(x0));";
    } else if (value.equals(Float.NaN)) {
      return "org.junit.Assert.assertTrue(Float.isNaN(x0));";
    }

    if (equalityMode.equals(EqualityMode.EQUALSMETHOD)) {
      StringBuilder b = new StringBuilder();
      b.append("org.junit.Assert.assertEquals(");
      // First add a message
      b.append("\"'\" + " + "x0" + " + \"' != '\" + ")
          .append(Value.toCodeString(value))
          .append("+ \"'\", ");
      b.append("x0");
      b.append(", ");
      b.append(Value.toCodeString(value));
      // Close assert.
      b.append(");");
      return b.toString();
    } else if (equalityMode.equals(EqualityMode.EQUALSEQUALS)) {
      StringBuilder b = new StringBuilder();
      b.append("org.junit.Assert.assertTrue(");
      b.append("\"'\" + " + "x0" + " + \"' != '\" + ")
          .append(Value.toCodeString(value))
          .append("+ \"'\", ");
      b.append("x0 == ").append(Value.toCodeString(value));
      b.append(");");
      return b.toString();
    } else {
      throw new Error("unexpected equalityMode " + equalityMode);
    }
  }

  @Override
  public String toCommentString() {
    return null;
  }
}
