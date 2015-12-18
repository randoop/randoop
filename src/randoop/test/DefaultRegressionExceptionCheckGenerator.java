package randoop.test;

import randoop.reflection.VisibilityPredicate;

/**
 * Default implementation of {@code RegressionExceptionCheckGenerator} providing
 * ability to get name of a visible exception class that allows a particular
 * exception to be caught. 
 */
public abstract class DefaultRegressionExceptionCheckGenerator implements RegressionExceptionCheckGenerator {
  
  private VisibilityPredicate visibility;

  public DefaultRegressionExceptionCheckGenerator(VisibilityPredicate visibility) {
    this.visibility = visibility;
  }

  /** 
   * Returns the nearest visible superclass -- usually the argument itself. 
   * @param clazz  the class for which superclass is needed
   * @return the nearest public class that is the argument or a superclass
   */
  // XXX is there a better home for this in reflection code?
  private Class<?> nearestVisibleSuperclass(Class<?> clazz) {
    while (! visibility.isVisible(clazz)) {
      clazz = clazz.getSuperclass();
    }
    return clazz;
  }

  /**
   * Returns the canonical name for the nearest visible class that will catch
   * an exception with the given class.
   * 
   * @param c  the exception class 
   * @return the nearest public visible, c or a superclass of c 
   */
  public String getCatchClassName(Class<? extends Throwable> c) {
    Class<?> catchClass = nearestVisibleSuperclass(c);
    return catchClass.getCanonicalName();
  }
}
