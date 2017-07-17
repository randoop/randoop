package randoop.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Methods that indicate whether a class, method, constructor, or field should be used in Randoop's
 * exploration.
 */
public interface ReflectionPredicate {

  /**
   * Indicate whether Randoop should use a class.
   *
   * @param c the class to test
   * @return true if the class meets the predicate criteria, false otherwise
   */
  boolean test(Class<?> c);

  /**
   * Indicate whether Randoop should use a method.
   *
   * @param m the method to test
   * @return true if the method meets the predicate criteria, false otherwise
   */
  boolean test(Method m);

  /**
   * Indicate whether Randoop should use a constructor.
   *
   * @param m the constructor to test
   * @return true if the constructor meets the predicate criteria, false otherwise
   */
  boolean test(Constructor<?> m);

  /**
   * Indicate whether Randoop should use a field.
   *
   * @param f the field to test
   * @return true if the field meets the predicate criteria, false otherwise
   */
  boolean test(Field f);
}
