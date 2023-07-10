package randoop.contract;

import java.lang.reflect.Array;
import java.util.Arrays;
import org.apache.commons.lang3.ArrayUtils;
import org.plumelib.util.StringsPlume;
import randoop.main.RandoopBug;
import randoop.sequence.Value;
import randoop.types.JavaTypes;
import randoop.types.PrimitiveTypes;
import randoop.types.TypeTuple;

/**
 * A check recording the current state of an array during execution
 *
 * <p>ObserverEqArray checks are not checks that must hold of all objects of a given class (unlike a
 * check like {@link EqualsReflexive}, which must hold for any objects, no matter its execution
 * context).
 */
public final class ObserverEqArray extends ObjectContract {

  /** The run-time result of calling the observer: an array of literals */
  public Object value;

  /** The maximum length of arrays in generated tests. */
  public static final int MAX_ARRAY_LENGTH = 25;

  /**
   * The maximum delta between the expected and actual for which both numbers (doubles or floats)
   * are still considered equal.
   */
  public static final double DELTA = 1e-15;

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

  /**
   * Create a new ObserverEqArray.
   *
   * @param value the run-time result of calling the observer: an array of literals
   */
  public ObserverEqArray(Object value) {
    this.value = value;
    if (!isLiteralType(value.getClass().getComponentType())) {
      throw new RandoopBug(
          String.format(
              "Cannot represent %s as a literal",
              StringsPlume.toStringAndClass(value.getClass().getComponentType())));
    }
  }

  /**
   * Returns true if the class (representing an array type) is a literal.
   *
   * @param cls -- the class to be tested
   * @return true iff the class is a primitive, boxed primitive, String, Class, or Enum
   */
  private boolean isLiteralType(Class<?> cls) {
    if (cls == Class.class || cls == String.class || cls.isEnum()) {
      return true;
    }
    return PrimitiveTypes.isBoxedPrimitive(cls) || cls.isPrimitive();
  }

  @Override
  public String toCodeString() {
    StringBuilder b = new StringBuilder();
    if (Array.getLength(value) < MAX_ARRAY_LENGTH) {
      if (value.getClass().getComponentType() == float.class
          || value.getClass().getComponentType() == double.class) {
        b.append(
            String.format("org.junit.Assert.assertArrayEquals(x0, %s, %s);", printArray(), DELTA));
      } else {
        b.append(String.format("org.junit.Assert.assertArrayEquals(x0, %s);", printArray()));
      }
    }
    return b.toString();
  }

  /**
   * Prints the code string of the second parameter (instantiation of array) in assertArrayEquals
   * e.g. prints the second parameter in assertArrayEquals(var, new int[] {1,2,3})
   *
   * @return String that represents an instantiation of an array equal to value
   */
  private String printArray() {
    String finalString = "";
    if (value == null) {
      finalString += "null";
    } else {
      finalString += "new ";
      finalString += value.getClass().getCanonicalName();
      finalString += printArrayComponents();
    }
    return finalString;
  }

  /**
   * Converts an Object into its array representation
   *
   * @param value - the given Object
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
   * Helper method that prints the components of the array
   *
   * @return String that represents the components of the array
   */
  private String printArrayComponents() {
    String finalString = "{";
    int length = Array.getLength(value);
    if (value.getClass().getComponentType().isEnum()) {
      for (int i = 0; i < length; i++) {
        if (Array.get(value, i) == null) {
          finalString += "null";
        } else {
          finalString += new EnumValue((Enum<?>) Array.get(value, i)).getValueName();
        }
        if (i < length - 1) {
          finalString += ", ";
        }
      }
    } else if (value.getClass().getComponentType() == Class.class) {
      for (int i = 0; i < length; i++) {
        if (Array.get(value, i) == null) {
          finalString += "null";
        } else {
          finalString += ((Class<?>) Array.get(value, i)).getCanonicalName() + ".class";
        }
        if (i < length - 1) {
          finalString += ", ";
        }
      }
    } else {
      for (int i = 0; i < length; i++) {
        finalString += Value.toCodeString(Array.get(value, i));
        if (i < length - 1) {
          finalString += ", ";
        }
      }
    }
    finalString += "}";
    return finalString;
  }

  @Override
  public boolean evaluate(Object... objects) throws Throwable {
    assert objects.length == 1;
    return Arrays.equals(toObjectArray(value), toObjectArray(objects[0]));
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
    return null;
  }

  @Override
  public String get_observer_str() {
    return "ObserverEqArray";
  }

  @Override
  public String toString() {
    return "randoop.ObserverEqArray, value=" + StringsPlume.escapeJava(printArrayComponents());
  }
}
