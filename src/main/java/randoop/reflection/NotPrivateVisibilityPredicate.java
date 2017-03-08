package randoop.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * NotPrivateVisibilityPredicate is a {@link VisibilityPredicate} that returns true in the case that
 * the class/method/constructor/field is not declared to be private.
 */
public class NotPrivateVisibilityPredicate implements VisibilityPredicate {

  /**
   * {@inheritDoc}
   *
   * @return true if the class access modifier is not private, and false, otherwise
   */
  @Override
  public boolean isVisible(Class<?> c) {
    return (c.getDeclaringClass() == null || isVisible(c.getDeclaringClass()))
        && isVisible(c.getModifiers() & Modifier.classModifiers());
  }

  /**
   * {@inheritDoc}
   *
   * @return true if the method access modifier is not private, and false, otherwise
   */
  @Override
  public boolean isVisible(Method m) {
    return isVisible(m.getModifiers() & Modifier.methodModifiers());
  }

  /**
   * {@inheritDoc}
   *
   * @return true if the constructor access modifier is not private, and false, otherwise
   */
  @Override
  public boolean isVisible(Constructor<?> c) {
    return isVisible(c.getModifiers() & Modifier.constructorModifiers());
  }

  /**
   * {@inheritDoc}
   *
   * @return true if the field access modifier is not private, and false, otherwise
   */
  @Override
  public boolean isVisible(Field f) {
    return isVisible(f.getModifiers() & Modifier.fieldModifiers());
  }

  /**
   * Returns true if the {@link java.lang.reflect.Modifier Modifier} value does not have private
   * set.
   *
   * @param mods the modifiers value
   * @return true if the private bit is not set, false otherwise
   */
  private boolean isVisible(int mods) {
    return !Modifier.isPrivate(mods);
  }
}
