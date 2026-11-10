package randoop.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/** A predicate that allows everything. */
public class EverythingAllowedPredicate implements ReflectionPredicate {

  /** Creates an EverythingAllowedPredicate. */
  public EverythingAllowedPredicate() {}

  @Override
  public boolean test(Class<?> c) {
    return true;
  }

  @Override
  public boolean test(Method m) {
    return true;
  }

  @Override
  public boolean test(Constructor<?> m) {
    return true;
  }

  @Override
  public boolean test(Field f) {
    return true;
  }
}
