package randoop.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * PublicVisibilityPredicate is a {@link VisibilityPredicate} that returns true
 * in the case that the class/method/constructor/field is public.
 *
 */
public class PublicVisibilityPredicate implements VisibilityPredicate {

  /**
   * {@inheritDoc}
   * @return true if class is declared public, false otherwise.
   */
  @Override
  public boolean isVisible(Class<?> c) {
    return isVisible(c.getModifiers() & Modifier.classModifiers());
  }

  /**
   * {@inheritDoc}
   * @return true if method is declared public, false otherwise.
   */
  @Override
  public boolean isVisible(Method m) {
    return isVisible(m.getModifiers() & Modifier.methodModifiers());
  }

  /**
   * {@inheritDoc}
   * @return true if constructor is declared public, false otherwise.
   */
  @Override
  public boolean isVisible(Constructor<?> c) {
    return isVisible(c.getModifiers() & Modifier.constructorModifiers());
  }

  /**
   * {@inheritDoc}
   * @return true if field is declared public, false otherwise.
   */
  @Override
  public boolean isVisible(Field f) {
    return isVisible(f.getModifiers() & Modifier.fieldModifiers());
  }
  
  /*
   * Checks whether the provided modifiers indicate public bit is set.
   */
  private boolean isVisible(int mods) {
    return Modifier.isPublic(mods);
  }

}
