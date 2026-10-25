package randoop.contract;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.StringJoiner;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.Assert;
import org.plumelib.util.StringsPlume;
import randoop.main.RandoopBug;
import randoop.reflection.AccessibilityPredicate;
import randoop.sequence.Value;
import randoop.types.JavaTypes;
import randoop.types.PrimitiveTypes;
import randoop.types.TypeTuple;

/**
 * A check recording the current state of an array during execution.
 *
 * <p>ObserverEqArray checks are not checks that must hold of all objects of a given class (unlike a
 * check like {@link EqualsReflexive}, which must hold for any objects, no matter its execution
 * context).
 */
public final class ObserverEqArray extends ObjectContract {

  /** The run-time result of calling the observer: an array of literals. */
  private Object value;

  /** The maximum difference for which doubles are considered equal. */
  private static final double DOUBLE_DELTA = 1e-15;

  /** The maximum difference for which floats are considered equal. */
  @SuppressWarnings("value:cast.unsafe") // value checker bug
  private static final float FLOAT_DELTA = (float) 1e-15;

  /**
   * Creates a new ObserverEqArray.
   *
   * @param value the run-time result of calling the observer: an array of literals
   * @param isAccessible the accessibility predicate
   */
  public ObserverEqArray(Object value, AccessibilityPredicate isAccessible) {
    this.value = value;
    if (!isLiteralType(value, isAccessible)) {
      throw new RandoopBug(
          String.format(
              "Cannot represent %s as a literal",
              StringsPlume.toStringAndClass(value.getClass().getComponentType())));
    }
  }

  /**
   * Returns true if the value can be represented as a literal in Java source code. If value is of
   * type Enum or another class that is not built-in, an accessibility check is performed.
   *
   * @param value an object
   * @param isAccessible the accessibility predicate
   * @return true iff the array elements are primitives, boxed primitives, Strings, Classes,
   *     accessible Enums or if the array consists of only null values
   */
  public static boolean isLiteralType(Object value, AccessibilityPredicate isAccessible) {
    Class<?> cls = value.getClass().getComponentType();
    if (cls == String.class || cls == Class.class) {
      return true;
    }
    if (PrimitiveTypes.isBoxedPrimitive(cls) || cls.isPrimitive()) {
      return true;
    }
    if (!isAccessible.isAccessible(cls)) {
      return false;
    }
    return cls.isEnum() || Arrays.stream((Object[]) value).allMatch(element -> element == null);
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof ObserverEqArray)) {
      return false;
    }
    ObserverEqArray other = (ObserverEqArray) o;
    // Do not apply DOUBLE_DELTA or FLOAT_DELTA for equality tests, only for `evaluate()`.
    if (!value.getClass().equals(other.value.getClass())) {
      return false;
    } else if (value instanceof byte[]) {
      return Arrays.equals((byte[]) value, (byte[]) other.value);
    } else if (value instanceof short[]) {
      return Arrays.equals((short[]) value, (short[]) other.value);
    } else if (value instanceof int[]) {
      return Arrays.equals((int[]) value, (int[]) other.value);
    } else if (value instanceof long[]) {
      return Arrays.equals((long[]) value, (long[]) other.value);
    } else if (value instanceof float[]) {
      return Arrays.equals((float[]) value, (float[]) other.value);
    } else if (value instanceof double[]) {
      return Arrays.equals((double[]) value, (double[]) other.value);
    } else if (value instanceof char[]) {
      return Arrays.equals((char[]) value, (char[]) other.value);
    } else if (value instanceof boolean[]) {
      return Arrays.equals((boolean[]) value, (boolean[]) other.value);
    } else {
      return Arrays.equals((Object[]) value, (Object[]) other.value);
    }
  }

  @Override
  public int hashCode() {
    if (value instanceof byte[]) {
      return Arrays.hashCode((byte[]) value);
    } else if (value instanceof short[]) {
      return Arrays.hashCode((short[]) value);
    } else if (value instanceof int[]) {
      return Arrays.hashCode((int[]) value);
    } else if (value instanceof long[]) {
      return Arrays.hashCode((long[]) value);
    } else if (value instanceof float[]) {
      return Arrays.hashCode((float[]) value);
    } else if (value instanceof double[]) {
      return Arrays.hashCode((double[]) value);
    } else if (value instanceof char[]) {
      return Arrays.hashCode((char[]) value);
    } else if (value instanceof boolean[]) {
      return Arrays.hashCode((boolean[]) value);
    } else {
      return Arrays.hashCode((Object[]) value);
    }
  }

  @Override
  public String toCodeString() {
    if (value.getClass().getComponentType() == double.class) {
      return String.format(
          "org.junit.Assert.assertArrayEquals(x0, %s, %s);", newArrayExpression(), DOUBLE_DELTA);
    } else if (value.getClass().getComponentType() == float.class) {
      return String.format(
          "org.junit.Assert.assertArrayEquals(x0, %s, (float)%s);",
          newArrayExpression(), FLOAT_DELTA);
    } else if (value.getClass().getComponentType() == boolean.class) {
      return String.format("assertBooleanArrayEquals(x0, %s);", newArrayExpression());
    } else {
      return String.format("org.junit.Assert.assertArrayEquals(x0, %s);", newArrayExpression());
    }
  }

  /**
   * Returns a Java array constructor expression for this, e.g., "new int[] {1,2,3}".
   *
   * @return a Java array constructor expression for this
   */
  private String newArrayExpression() {
    return "new " + value.getClass().getCanonicalName() + arrayComponentsToJavaString();
  }

  /**
   * Returns a string representation of the components of the array, enclosed in curly braces, as it
   * would appear in Java source code.
   *
   * @return a String that represents the components of the array
   */
  private String arrayComponentsToJavaString() {
    StringJoiner sj = new StringJoiner(", ", "{", "}");
    int length = Array.getLength(value);
    for (int i = 0; i < length; i++) {
      sj.add(Value.toCodeString(Array.get(value, i)));
    }
    return sj.toString();
  }

  @Override
  @SuppressWarnings("AssertionFailureIgnored") // return false if assertion error is thrown
  public boolean evaluate(Object... objects) throws Throwable {
    assert objects.length == 1;
    try {
      if (!value.getClass().equals(objects[0].getClass())) {
        return false;
      } else if (value instanceof double[]) {
        Assert.assertArrayEquals((double[]) value, (double[]) objects[0], DOUBLE_DELTA);
      } else if (value instanceof float[]) {
        Assert.assertArrayEquals((float[]) value, (float[]) objects[0], FLOAT_DELTA);
      } else if (value instanceof byte[]) {
        Assert.assertArrayEquals((byte[]) value, (byte[]) objects[0]);
      } else if (value instanceof short[]) {
        Assert.assertArrayEquals((short[]) value, (short[]) objects[0]);
      } else if (value instanceof int[]) {
        Assert.assertArrayEquals((int[]) value, (int[]) objects[0]);
      } else if (value instanceof long[]) {
        Assert.assertArrayEquals((long[]) value, (long[]) objects[0]);
      } else if (value instanceof boolean[]) {
        Assert.assertArrayEquals((boolean[]) value, (boolean[]) objects[0]);
      } else if (value instanceof char[]) {
        Assert.assertArrayEquals((char[]) value, (char[]) objects[0]);
      } else {
        Assert.assertArrayEquals((Object[]) value, (Object[]) objects[0]);
      }
    } catch (AssertionError e) {
      return false;
    }
    return true;
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
    return "ObserverEqArray";
  }

  @Override
  public String toString() {
    return "ObserverEqArray(" + arrayComponentsToJavaString() + ")";
  }
}
