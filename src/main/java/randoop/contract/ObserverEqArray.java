package randoop.contract;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.StringJoiner;
import org.apache.commons.lang3.ArrayUtils;
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

  /**
   * The maximum delta between the expected and actual for which both numbers (doubles or floats)
   * are still considered equal.
   */
  private static final double FLOATING_POINT_DELTA = 1e-15;

  /**
   * Creates a new ObserverEqArray.
   *
   * @param value the run-time result of calling the observer: an array of literals
   * @param isAccessible the accessibility predicate
   */
  public ObserverEqArray(Object value, AccessibilityPredicate isAccessible) {
    this.value = value;
    if (!isLiteralType(value.getClass().getComponentType(), isAccessible)) {
      throw new RandoopBug(
          String.format(
              "Cannot represent %s as a literal",
              StringsPlume.toStringAndClass(value.getClass().getComponentType())));
    }
  }

  /**
   * Returns true if values of the class can be represented as literals in Java source code.
   *
   * @param cls a class
   * @param isAccessible the accessibility predicate
   * @return true iff the class is a primitive, boxed primitive, String, Class, or accessible Enum
   */
  public static boolean isLiteralType(Class<?> cls, AccessibilityPredicate isAccessible) {
    if (cls == Class.class || cls == String.class) {
      return true;
    }
    if (cls.isEnum() && isAccessible.isAccessible(cls)) {
      return true;
    }
    return PrimitiveTypes.isBoxedPrimitive(cls) || cls.isPrimitive();
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof ObserverEqArray)) {
      return false;
    }
    ObserverEqArray other = (ObserverEqArray) o;
    return Arrays.equals(toObjectArray(value), toObjectArray(other.value));
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(toObjectArray(value));
  }

  @Override
  public String toCodeString() {
    if (value.getClass().getComponentType() == float.class
        || value.getClass().getComponentType() == double.class) {
      return String.format(
          "org.junit.Assert.assertArrayEquals(x0, %s, %s);",
          newArrayExpression(), FLOATING_POINT_DELTA);
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
    return "new " + value.getClass().getCanonicalName() + arrayComponentsToString();
  }

  /**
   * Converts an Object into its array representation.
   *
   * @param value the given Object
   * @return its array representation
   */
  private Object[] toObjectArray(Object value) {
    if (value instanceof byte[]) {
      return ArrayUtils.toObject((byte[]) value);
    } else if (value instanceof short[]) {
      return ArrayUtils.toObject((short[]) value);
    } else if (value instanceof int[]) {
      return ArrayUtils.toObject((int[]) value);
    } else if (value instanceof long[]) {
      return ArrayUtils.toObject((long[]) value);
    } else if (value instanceof float[]) {
      return ArrayUtils.toObject((float[]) value);
    } else if (value instanceof double[]) {
      return ArrayUtils.toObject((double[]) value);
    } else if (value instanceof char[]) {
      return ArrayUtils.toObject((char[]) value);
    } else if (value instanceof boolean[]) {
      return ArrayUtils.toObject((boolean[]) value);
    } else {
      return (Object[]) value;
    }
  }

  /**
   * Returns a string representation of the components of the array, enclosed in curly braces, as it
   * would appear in Java source code.
   *
   * @return a String that represents the components of the array
   */
  private String arrayComponentsToString() {
    StringJoiner sj = new StringJoiner(", ", "{", "}");
    int length = Array.getLength(value);
    for (int i = 0; i < length; i++) {
      sj.add(literalValueToString(Array.get(value, i)));
    }
    return sj.toString();
  }

  /**
   * Returns one literal value, as it would appear in Java code
   *
   * @return the Java code for the literal value
   */
  private String literalValueToString(Object element) {
    if (element == null) {
      return "null";
    } else if (element.getClass().isEnum()) {
      return new EnumValue((Enum<?>) element).getValueName();
    } else if (element.getClass() == Class.class) {
      return ((Class<?>) element).getCanonicalName() + ".class";
    } else {
      return Value.toCodeString(element);
    }
  }

  @Override
  @SuppressWarnings("AssertionFailureIgnored")
  public boolean evaluate(Object... objects) throws Throwable {
    assert objects.length == 1;
    try {
      if (value instanceof double[]) {
        Assert.assertArrayEquals((double[]) value, (double[]) objects[0], FLOATING_POINT_DELTA);
      } else if (value instanceof float[]) {
        Assert.assertArrayEquals(
            (float[]) value, (float[]) objects[0], (float) FLOATING_POINT_DELTA);
      } else {
        Assert.assertArrayEquals(toObjectArray(value), toObjectArray(objects[0]));
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
    return "ObserverEqArray(" + arrayComponentsToString() + ")";
  }
}
