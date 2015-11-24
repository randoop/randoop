package randoop.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Interface for predicates that check whether a class or class member is considered
 * visible.
 * @see randoop.main.GenInputsAbstract#public_only
 *
 */
public interface VisibilityPredicate {
  
  /**
   * Determines whether a {@link Class} object is considered visible.
   * @param c the class object to check
   * @return true if predicate criteria determines the class is visible, false otherwise.
   */
  boolean isVisible(Class<?> c);

  /**
   * Determines whether a {@link Method} object is considered visible.
   * @param m the Method object to check
   * @return true if predicate criteria determines the method is visible, false otherwise.
   */
  boolean isVisible(Method m);

  /**
   * Determines whether a {@link Constructor} object is considered visible.
   * @param c the constructor object to check
   * @return true if predicate criteria determines the constructor is visible, false otherwise.
   */
  boolean isVisible(Constructor<?> c);

  /**
   * Determines whether a {@link Field} object is considered visible.
   * @param f the field object to check
   * @return true if predicate criteria determines the field is visible, false otherwise.
   */
  boolean isVisible(Field f);
}
