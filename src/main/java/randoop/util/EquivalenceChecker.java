package randoop.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for checking equivalence between objects in randoop.generation.Detective. Mainly
 * for handling boxing and unboxing of primitive types.
 */
public class EquivalenceChecker {
  // Mapping of primitive types to their corresponding boxed types
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

  // Check whether two types are equivalent, taking into account boxing and unboxing
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
