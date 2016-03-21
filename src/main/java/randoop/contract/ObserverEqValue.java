package randoop.contract;

import java.io.ObjectStreamException;
import java.lang.reflect.Method;

import randoop.Globals;
import randoop.util.PrimitiveTypes;
import randoop.util.Util;

/**
 *
 * A check recording the value that an observer method returned during
 * execution, e.g. a check recording that a collection's <code>size()</code>
 * method returned <code>3</code> when called in particular sequence.
 *
 * <p>
 *
 * ObserverEqValue checks are not checks that must hold of all objects of a
 * given class (unlike a check like {@link EqualsReflexive}, which must hold for
 * any objects, no matter its execution context). Randoop creates an instance of
 * this contract when, during execution of a sequence, it determines that the
 * above property holds. The property thus represents a <i>regression</i> as it
 * captures the behavior of the code when it is executed.
 *
 */
public final class ObserverEqValue implements ObjectContract {

  /**
   * The observer method.
   */
  public Method observer;

  /**
   * The runtime value of the observer. This variable holds a primitive value or
   * String.
   */
  public Object value;

  @Override
  public boolean equals(Object o) {
    if (o == null) return false;
    if (o == this) return true;
    if (!(o instanceof ObserverEqValue)) {
      return false;
    }
    ObserverEqValue other = (ObserverEqValue) o;
    return observer.equals(other.observer) && Util.equalsWithNull(value, other.value);
  }

  @Override
  public int hashCode() {
    int h = 7;
    h = h * 31 + observer.hashCode();
    h = h * 31 + (value == null ? 0 : value.hashCode());
    return h;
  }

  public ObserverEqValue(Method observer, Object value) {
    this.observer = observer;
    this.value = value;
    assert (this.value == null)
            || PrimitiveTypes.isBoxedPrimitiveTypeOrString(this.value.getClass())
        : "obs value/class = "
            + this.value
            + "/"
            + this.value.getClass()
            + " observer = "
            + observer;
  }

  public String toCodeStringPreStatement() {
    return "";
  }

  @Override
  public String toCodeString() {
    StringBuilder b = new StringBuilder();
    b.append(Globals.lineSep);
    b.append(
        "// Regression assertion (captures the current behavior of the code)" + Globals.lineSep);

    String methodname = observer.getName();
    if (value == null) {
      b.append(String.format("assertNull(\"x0.%s() == null\", x0.%s());", methodname, methodname));
    } else if (observer.getReturnType().isPrimitive()
        && (!value.equals(Double.NaN))
        && (!value.equals(Float.NaN))) {
      if (observer.getReturnType().equals(boolean.class)) {
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
                methodname,
                PrimitiveTypes.toCodeString(value)));
      }
    } else { // string
      // System.out.printf ("value = %s - %s\n", value, value.getClass());
      b.append(
          String.format(
              "org.junit.Assert.assertEquals(x0.%s(), %s);",
              methodname,
              PrimitiveTypes.toCodeString(value)));
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

  @Override
  public String toCommentString() {
    return toCodeString();
  }

  @Override
  public boolean evalExceptionMeansFailure() {
    return true;
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
