package randoop.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Methods that declare whether a reflection object
 * should be used in randoop's exploration.
 *
 */
public interface ReflectionFilter {

  public boolean canUse(Class<?> c);
  public boolean canUse(Method m);
  public boolean canUse(Constructor<?> m);


}
