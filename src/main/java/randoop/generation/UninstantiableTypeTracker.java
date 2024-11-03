package randoop.generation;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import randoop.types.Type;

/**
 * Tracks types that cannot be instantiated due to the absence of producer methods. Used to avoid
 * generating sequences through {@link randoop.generation.DemandDrivenInputCreator} for such types.
 */
public class UninstantiableTypeTracker {
  /** Types that cannot be instantiated due to the absence of producer methods. */
  private static final Set<Type> uninstantiableTypes = new HashSet<>();

  /**
   * Adds a type to the set of uninstantiable types.
   *
   * @param type the type to add
   */
  public static void addType(Type type) {
    uninstantiableTypes.add(type);
  }

  /**
   * Checks if a type is marked as uninstantiable.
   *
   * @param type the type to check
   * @return true if the type is uninstantiable, false otherwise
   */
  public static boolean contains(Type type) {
    return uninstantiableTypes.contains(type);
  }

  /**
   * Retrieves all uninstantiable types.
   *
   * @return an unmodifiable set of uninstantiable types
   */
  public static Set<Type> getUninstantiableTypes() {
    return Collections.unmodifiableSet(uninstantiableTypes);
  }
}
