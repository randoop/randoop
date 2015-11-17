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

  @Override
  public boolean isVisible(Class<?> c) {
    return isVisible(c.getModifiers());
  }

  @Override
  public boolean isVisible(Method m) {
    return isVisible(m.getModifiers());
  }

  @Override
  public boolean isVisible(Constructor<?> c) {
    return isVisible(c.getModifiers());
  }

  @Override
  public boolean isVisible(Field f) {
    return isVisible(f.getModifiers());
  }
  
  private boolean isVisible(int mods) {
    return !Modifier.isPrivate(mods);
  }

}
