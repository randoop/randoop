package randoop.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import randoop.reflection.ReflectionPredicate;

public class EverythingAllowedPredicate implements ReflectionPredicate {

  public boolean test(Class<?> c) {
    return true;
  }

  public boolean test(Method m) {
    return true;
  }

  public boolean test(Constructor<?> m) {
    return true;
  }

  @Override
  public boolean test(Field f) {
    return true;
  }

}
