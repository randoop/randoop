package randoop.contract;

import java.lang.reflect.Executable;
import java.util.Arrays;
import java.util.Objects;
import org.plumelib.util.StringsPlume;
import randoop.main.RandoopBug;
import randoop.operation.CallableOperation;
import randoop.operation.TypedOperation;
import randoop.sequence.Value;
import randoop.types.JavaTypes;
import randoop.types.Type;
import randoop.types.TypeTuple;

/**
 * A check recording the value that an observer method returned during execution, e.g. a check
 * recording that a collection's {@code size()} method returned {@code 3}.
 *
 * <p>ObserverEqValue checks are not checks that must hold of all objects of a given class (unlike a
 * check like {@link EqualsReflexive}, which must hold for any objects, no matter its execution
 * context).
 */
public final class ObserverEqValue extends ObjectContract {

  /** The observer method. */
  public TypedOperation observer;

  /** The run-time result of calling the observer: a primitive value or String. */
  public Object value;

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof ObserverEqValue)) {
      return false;
    }
    ObserverEqValue other = (ObserverEqValue) o;
    return observer.equals(other.observer) && Objects.equals(value, other.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(observer, value);
  }

  /**
   * Create a new ObserverEqValue.
   *
   * @param observer the observer method
   * @param value the run-time result of calling the observer: a primitive value or String
   */
  public ObserverEqValue(TypedOperation observer, Object value) {
    assert observer.isMethodCall() || observer.isConstructorCall()
        : "Observer must be MethodCall or ConstructorCall, got " + observer;
    this.observer = observer;
    this.value = value;
    if (!isLiteralValue(value)) {
      throw new RandoopBug(
          String.format(
              "Cannot represent %s as a literal; observer = %s",
              StringsPlume.toStringAndClass(value), observer));
    }
  }

  /**
   * Returns true if the argument can be written as a literal in Java source code.
   *
   * @param value the value to be tested
   * @return true if the argument can be written as a literal in Java source code
   */
  public static boolean isLiteralValue(Object value) {
    if (value == null) {
      return true;
    }
    Class<?> cls = value.getClass();
    if (cls == Class.class || cls == String.class || cls.isEnum()) {
      return true;
    }
    Type type = Type.forClass(cls);
    if (type.isBoxedPrimitive()) {
      return true;
    }
    return false;
  }

  @Override
  public String toCodeString() {
    StringBuilder b = new StringBuilder();

    // It might be nicer to call TypedOperation.getOperation().appendCode(...) to obtain the printed
    // representation, but this works for this simple case.
    String call;
    {
      CallableOperation operation = observer.getOperation();
      String methodname = operation.getName();
      if (operation.isStatic()) {
        Executable m = (Executable) operation.getReflectionObject();
        String theClass = m.getDeclaringClass().getName();
        call = String.format("%s.%s(x0)", theClass, methodname);
      } else {
        call = String.format("x0.%s()", methodname);
      }
    }

    if (value == null) {
      b.append(String.format("org.junit.Assert.assertNull(\"%s == null\", %s);", call, call));
    } else if (observer.getOutputType().runtimeClassIs(boolean.class)) {
      assert value.equals(true) || value.equals(false);
      if (value.equals(true)) {
        b.append(String.format("org.junit.Assert.assertTrue(%s);", call));
      } else {
        b.append(String.format("org.junit.Assert.assertFalse(%s);", call));
      }
    } else if (observer.getOutputType().isPrimitive()
        && !value.equals(Double.NaN)
        && !value.equals(Float.NaN)) {
      b.append(
          String.format("org.junit.Assert.assertEquals(%s, %s);", call, Value.toCodeString(value)));
    } else { // string
      // System.out.printf("value = %s - %s%n", value, value.getClass());
      b.append(
          String.format("org.junit.Assert.assertEquals(%s, %s);", call, Value.toCodeString(value)));
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

  /** The arguments to which this contract can be applied. */
  static TypeTuple inputTypes = new TypeTuple(Arrays.asList(JavaTypes.OBJECT_TYPE));

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
    return String.format(
        "<ObserverEqValue %s, value = '%s'", observer, StringsPlume.toStringAndClass(value));
  }
}
