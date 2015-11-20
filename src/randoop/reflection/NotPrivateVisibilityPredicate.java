package randoop.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * NotPrivateVisibilityPredicate is a {@link VisibilityPredicate} that returns
 * true in the case that the class/method/constructor/field is not declared to
 * be private.
 * 
 * @author bjkeller
 *
 */
public class NotPrivateVisibilityPredicate implements VisibilityPredicate {

  /**
   * {@inheritDoc}
   * @return true if the class access modifier is not private, and false, otherwise.
   */
  @Override
  public boolean isVisible(Class<?> c) {
    return isVisible(c.getModifiers());
  }

  /**
   * {@inheritDoc}
   * @return true if the method access modifier is not private, and false, otherwise.
   */
  @Override
  public boolean isVisible(Method m) {
    return isVisible(m.getModifiers());
  }

  /**
   * {@inheritDoc}
   * @return true if the constructor access modifier is not private, and false, otherwise.
   */
  @Override
  public boolean isVisible(Constructor<?> c) {
    return isVisible(c.getModifiers());
  }

  /**
   * {@inheritDoc}
   * @return true if the field access modifier is not private, and false, otherwise.
   */
  @Override
  public boolean isVisible(Field f) {
    return isVisible(f.getModifiers());
  }
  
  private boolean isVisible(int mods) {
    return !Modifier.isPrivate(mods);
  }

}
