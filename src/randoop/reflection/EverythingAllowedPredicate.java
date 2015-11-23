package randoop.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class EverythingAllowedPredicate implements ReflectionPredicate {

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
