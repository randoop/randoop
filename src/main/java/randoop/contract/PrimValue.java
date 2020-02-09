package randoop.contract;

import java.util.Arrays;
import java.util.Objects;
import randoop.Globals;
import randoop.sequence.StringTooLongException;
import randoop.sequence.Value;
import randoop.types.JavaTypes;
import randoop.types.Type;
import randoop.types.TypeTuple;

/**
 * A check recording the value of a primitive value (or String) obtained during execution, (e.g.
 * {@code var3 == 1} where {@code var3} is an integer-valued variable in a Randoop test).
 *
 * <p>Obviously, this is not a property that must hold of all objects in a test. Randoop creates an
 * instance of this contract when, during execution of a sequence, it determines that the above
 * property holds. The property thus represents a <i>regression</i> as it captures the behavior of
 * the code when it is executed.
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
    return "randoop.PrimValue, value=" + value;
  }

  @Override
  public String get_observer_str() {
    return "PrimValue";
  }

  @Override
  public String toCodeString() {

    StringBuilder b = new StringBuilder();
    b.append(Globals.lineSep);
    b.append("// Regression assertion (captures the current behavior of the code)")
        .append(Globals.lineSep);

    // ValueExpression represents the value of a variable.
    // We special-case printing for this type of expression,
    // to improve readability.
    if (value.equals(Double.NaN) || value.equals(Float.NaN)) {
      b.append("org.junit.Assert.assertEquals(");
      if (value.equals(Double.NaN)) {
        b.append("(double)");
      } else {
        b.append("(float)");
      }
      b.append("x0");
      b.append(", ");
      b.append(Value.toCodeString(value));
      b.append(", 0);");
    } else if (equalityMode.equals(EqualityMode.EQUALSMETHOD)) {
      b.append("org.junit.Assert.assertTrue(");
      // First add a message
      b.append("\"'\" + " + "x0" + " + \"' != '\" + ")
          .append(Value.toCodeString(value))
          .append("+ \"'\", ");
      b.append("x0");
      b.append(".equals(");
      b.append(Value.toCodeString(value));
      b.append(")");
      // Close assert.
      b.append(");");
    } else {
      assert equalityMode.equals(EqualityMode.EQUALSEQUALS);
      b.append("org.junit.Assert.assertTrue(");
      b.append("\"'\" + " + "x0" + " + \"' != '\" + ")
          .append(Value.toCodeString(value))
          .append("+ \"'\", ");
      b.append("x0 == ").append(Value.toCodeString(value));
      b.append(");");
    }

    return b.toString();
  }

  @Override
  public String toCommentString() {
    return null;
  }
}
