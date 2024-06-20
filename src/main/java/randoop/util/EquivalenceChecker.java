package randoop.util;

import randoop.types.PrimitiveTypes;

/**
 * Utility class for checking equivalence between objects in randoop.generation.Detective. Mainly
 * for handling boxing and unboxing of primitive types.
 */
public class EquivalenceChecker {
  /**
   * Determines if two Class objects represent equivalent types, considering both primitive types
   * and their boxed counterparts as equivalent. For instance, int.class and Integer.class are
   * considered equivalent.
   *
   * @param c1 the first class to compare
   * @param c2 the second class to compare
   * @return true if the classes represent equivalent types, false otherwise
   */
  public static boolean areEquivalentTypesConsideringBoxing(Class<?> c1, Class<?> c2) {
    if (c1.equals(c2)) {
      return true;
    }
    if (c1.isPrimitive()) {
      return c2.equals(PrimitiveTypes.toBoxedType(c1));
    }
    if (c2.isPrimitive()) {
      return c1.equals(PrimitiveTypes.toBoxedType(c2));
    }
    return false;
  }
}
