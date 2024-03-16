package randoop.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for checking equivalence between objects in randoop.generation.Detective. Mainly
 * for handling boxing and unboxing of primitive types.
 */
public class EquivalenceChecker {
  /** The mapping of primitive types to their corresponding boxed types. */
  private static final Map<Class<?>, Class<?>> PRIMITIVE_TO_BOXED;

  static {
    Map<Class<?>, Class<?>> map = new HashMap<>();
    map.put(boolean.class, Boolean.class);
    map.put(byte.class, Byte.class);
    map.put(char.class, Character.class);
    map.put(double.class, Double.class);
    map.put(float.class, Float.class);
    map.put(int.class, Integer.class);
    map.put(long.class, Long.class);
    map.put(short.class, Short.class);
    map.put(void.class, Void.class);
    PRIMITIVE_TO_BOXED = Collections.unmodifiableMap(map);
  }

  /**
   * Determines if two Class objects represent equivalent types, considering
   * both primitive types and their boxed counterparts as equivalent. For instance,
   * int.class and Integer.class are considered equivalent.
   *
   * @param c1 the first class to compare
   * @param c2 the second class to compare
   * @return true if the classes represent equivalent types, false otherwise
   */
  public static boolean equivalentTypes(Class<?> c1, Class<?> c2) {
    if (c1.equals(c2)) {
      return true;
    }
    if (c1.isPrimitive()) {
      return c2.equals(PRIMITIVE_TO_BOXED.get(c1));
    }
    if (c2.isPrimitive()) {
      return c1.equals(PRIMITIVE_TO_BOXED.get(c2));
    }
    return false;
  }
}
