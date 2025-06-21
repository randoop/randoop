package randoop.util;

import randoop.types.PrimitiveTypes;
import randoop.types.Type;

/**
 * Utility class for checking type equivalencies between objects in {@link
 * randoop.generation.DemandDrivenInputCreator}.
 */
public class TypeEquivalenceChecker {

  /** Do not instantiate. */
  private TypeEquivalenceChecker() {
    throw new Error("Do not instantiate");
  }

  /**
   * Determine if two types are equivalent. Consider both primitive types and their boxed
   * counterparts as equivalent. For instance, {@code int.class} is equivalent to {@code
   * Integer.class}.
   *
   * @param t1 the first type to compare
   * @param t2 the second type to compare
   * @return true if the types are equivalent, false otherwise
   */
  public static boolean equalsOrBoxed(Type t1, Type t2) {
    if (t1.equals(t2)) {
      return true;
    }

    if (t1.isPrimitive()) {
      return t2.getRuntimeClass().equals(PrimitiveTypes.toBoxedType(t1.getRuntimeClass()));
    } else if (t2.isPrimitive()) {
      return t1.getRuntimeClass().equals(PrimitiveTypes.toBoxedType(t2.getRuntimeClass()));
    }

    // TODO: Check subtyping relationships between classes.

    return false;
  }
}
