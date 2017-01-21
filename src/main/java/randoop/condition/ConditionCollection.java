package randoop.condition;

import java.lang.reflect.AccessibleObject;
import java.util.List;
import java.util.Map;

import randoop.types.ClassOrInterfaceType;

/**
 * Represents a collection of preconditions and throws-conditions.
 * Preconditions are represented by {@link Condition} objects, while throws-conditions are
 * represented by (condition, exception-type) pairs.
 */
public interface ConditionCollection {

  /**
   * Returns the preconditions for the given method or constructor.
   *
   * @param member  either a {@code java.lang.reflect.Method} or {@code java.lang.reflect.ConstructorCall}
   * @return the list of preconditions for the given method or constructor
   */
  List<Condition> getPreconditions(AccessibleObject member);

  /**
   * Returns the throws-conditions for the given method or constructor.
   *
   * @param member  either a {@code java.lang.reflect.Method} or {@code java.lang.reflect.ConstructorCall}
   * @return the list of throws conditions for the given method or constructor
   */
  Map<Condition, ClassOrInterfaceType> getThrowsConditions(AccessibleObject member);
}
