package randoop.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class EverythingAllowedFilter implements ReflectionFilter {

  public boolean canUse(Class<?> c) {
    return true;
  }

  public boolean canUse(Method m) {
    return true;
  }

  public boolean canUse(Constructor<?> m) {
    return true;
  }

  @Override
  public boolean canUse(Field f) {
    return true;
  }

}
