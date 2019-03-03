package randoop.contract;

import java.util.Arrays;
import java.util.Objects;
import randoop.Globals;
import randoop.operation.TypedOperation;
import randoop.sequence.Value;
import randoop.types.JavaTypes;
import randoop.types.Type;
import randoop.types.TypeTuple;
import randoop.util.Util;

/**
 * A check recording the value that an observer method returned during execution, e.g. a check
 * recording that a collection's {@code size()} method returned {@code 3} when called in particular
 * sequence.
 *
 * <p>ObserverEqValue checks are not checks that must hold of all objects of a given class (unlike a
 * check like {@link EqualsReflexive}, which must hold for any objects, no matter its execution
 * context). Randoop creates an instance of this contract when, during execution of a sequence, it
 * determines that the above property holds. The property thus represents a <i>regression</i> as it
 * captures the behavior of the code when it is executed.
 */
public final class ObserverEqValue extends ObjectContract {

  /** The observer method. */
  public TypedOperation observer;

  /** The run-time value of the observer. This variable holds a primitive value or String. */
  public Object value;

  @Override
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (o == this) {
      return true;
    }
    if (!(o instanceof ObserverEqValue)) {
      return false;
    }
    ObserverEqValue other = (ObserverEqValue) o;
    return observer.equals(other.observer) && Util.equalsWithNull(value, other.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(observer, value);
  }

  public ObserverEqValue(TypedOperation observer, Object value) {
    assert observer.isMethodCall() : "Observer must be MethodCall, got " + observer;
    this.observer = observer;
    this.value = value;
    assert isLiteralValue(value)
        : String.format(
            "Cannot represent %s [%s] as a literal; observer = %s",
            value, value.getClass(), observer);
  }

  /**
   * Returns true if the argument can be written as a literal in Java source code.
   *
   * @param value the value to be tested
   * @return true if the argument can be written as a literal in Java source code
   */
  public static boolean isLiteralValue(Object value) {
    if (value == null) {
      return false;
    }
    Type type = Type.forClass(value.getClass());
    return type.isBoxedPrimitive() || type.isClass() || type.isString();
  }

  @Override
  public String toCodeString() {
    StringBuilder b = new StringBuilder();
    b.append(Globals.lineSep);
    b.append("// Regression assertion (captures the current behavior of the code)")
        .append(Globals.lineSep);

    String methodname = observer.getOperation().getName();
    if (value == null) {
      b.append(String.format("assertNull(\"x0.%s() == null\", x0.%s());", methodname, methodname));
    } else if (observer.getOutputType().isPrimitive()
        && !value.equals(Double.NaN)
        && !value.equals(Float.NaN)) {
      if (observer.getOutputType().runtimeClassIs(boolean.class)) {
        assert value.equals(true) || value.equals(false);
        if (value.equals(true)) {
          b.append(String.format("org.junit.Assert.assertTrue(x0.%s());", methodname));
        } else {
          b.append(String.format("org.junit.Assert.assertFalse(x0.%s());", methodname));
        }
      } else {
        b.append(
            String.format(
                "org.junit.Assert.assertTrue(x0.%s() == %s);",
                methodname, Value.toCodeString(value)));
      }
    } else { // string
      // System.out.printf("value = %s - %s%n", value, value.getClass());
      b.append(
          String.format(
              "org.junit.Assert.assertEquals(x0.%s(), %s);",
              methodname, Value.toCodeString(value)));
    }
    return b.toString();
  }

  @Override
  public boolean evaluate(Object... objects) throws Throwable {
    assert objects.length == 0;
    throw new RuntimeException("not implemented.");
  }

  @Override
  public int getArity() {
    return 1;
  }

  static TypeTuple inputTypes = new TypeTuple(Arrays.<Type>asList(JavaTypes.OBJECT_TYPE));

  @Override
  public TypeTuple getInputTypes() {
    return inputTypes;
  }

  @Override
  public String toCommentString() {
    return toCodeString();
  }

  @Override
  public String get_observer_str() {
    return observer.toString();
  }

  @Override
  public String toString() {
    return String.format("<ObserverEqValue %s, value = '%s'", observer, value);
  }
}
