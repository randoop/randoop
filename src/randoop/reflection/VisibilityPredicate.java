package randoop.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public interface VisibilityPredicate {
  boolean isVisible(Class<?> c);

  boolean isVisible(Method m);

  boolean isVisible(Constructor<?> c);

  boolean isVisible(Field f);
}
