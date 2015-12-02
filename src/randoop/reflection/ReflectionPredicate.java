package randoop.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Methods that declare whether a reflection object
 * should be used in randoop's exploration.
 *
 */
public interface ReflectionPredicate {

  /**
   * Indicate whether a class meets the use criteria for predicate.
   * 
   * @param c  the class to test.
   * @return true if the class meets the predicate criteria, false, otherwise.
   */
  public boolean test(Class<?> c);
  
  /**
   * Indicate whether a method meets the use criteria for the predicate.
   * 
   * @param m  the method to test.
   * @return true if the method meets the predicate criteria, false, otherwise.
   */
  public boolean test(Method m);
  
  /**
   * Indicate whether a constructor meets the use criteria for the predicate.
   * 
   * @param m  the constructor to test.
   * @return true if the constructor meets the predicate criteria, false, otherwise.
   */
  public boolean test(Constructor<?> m);
  
  /**
   * Indicate whether a field meets the criteria for the predicate.
   * 
   * @param f  the field to test.
   * @return true if the field meets the predicate criteria, false, otherwise.
   */
  public boolean test(Field f);

}
