package randoop.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/** Interface for predicates that check whether a class or class member is considered visible. */
public abstract class VisibilityPredicate {

  /** A predicate that returns true for public elements. */
  public static VisibilityPredicate IS_PUBLIC = new PublicVisibilityPredicate();
  /** A predicate that returns true for non-private elements. */
  public static VisibilityPredicate IS_NOT_PRIVATE = new NotPrivateVisibilityPredicate();
  /** A predicate that always returns true. */
  public static VisibilityPredicate IS_ANY = new AnyVisibilityPredicate();

  /**
   * Determines whether this VisibilityPredicate considers a {@link Class} visible.
   *
   * @param c the class object to check
   * @return whether this considers the class to be visible
   */
  public abstract boolean isVisible(Class<?> c);

  /**
   * Determines whether this VisibilityPredicate considers a {@link Method} visible. Does not test
   * the visibility of the containing class.
   *
   * @param m the Method object to check
   * @return whether this considers the method to be visible
   */
  public abstract boolean isVisible(Method m);

  /**
   * Determines whether this VisibilityPredicate considers a {@link Constructor} visible. Does not
   * test the visibility of the containing class.
   *
   * @param c the constructor object to check
   * @return whether this considers the constructor to be visible
   */
  public abstract boolean isVisible(Constructor<?> c);

  /**
   * Determines whether this VisibilityPredicate considers a {@link Field} visible. Does not test
   * the visibility of the containing class.
   *
   * @param f the field object to check
   * @return whether this considers the field to be visible
   */
  public abstract boolean isVisible(Field f);

  /** AnyVisibilityPredicate is a {@link VisibilityPredicate} that always returns true. */
  public static class AnyVisibilityPredicate extends VisibilityPredicate {

    /**
     * {@inheritDoc}
     *
     * @return true
     */
    @Override
    public boolean isVisible(Class<?> c) {
      return true;
    }

    /**
     * {@inheritDoc}
     *
     * @return true
     */
    @Override
    public boolean isVisible(Method m) {
      return true;
    }

    /**
     * {@inheritDoc}
     *
     * @return true
     */
    @Override
    public boolean isVisible(Constructor<?> c) {
      return true;
    }

    /**
     * {@inheritDoc}
     *
     * @return true
     */
    @Override
    public boolean isVisible(Field f) {
      return true;
    }
  }

  /**
   * PublicVisibilityPredicate is a {@link VisibilityPredicate} that returns true in the case that
   * the class/method/constructor/field is public.
   */
  public static class PublicVisibilityPredicate extends VisibilityPredicate {

    /**
     * {@inheritDoc}
     *
     * @return true if class is declared public, false otherwise
     */
    @Override
    public boolean isVisible(Class<?> c) {
      return (c.getDeclaringClass() == null || isVisible(c.getDeclaringClass()))
          && isVisible(c.getModifiers() & Modifier.classModifiers());
    }

    /**
     * {@inheritDoc}
     *
     * @return true if method is declared public, false otherwise
     */
    @Override
    public boolean isVisible(Method m) {
      return isVisible(m.getModifiers() & Modifier.methodModifiers());
    }

    /**
     * {@inheritDoc}
     *
     * @return true if constructor is declared public, false otherwise
     */
    @Override
    public boolean isVisible(Constructor<?> c) {
      return isVisible(c.getModifiers() & Modifier.constructorModifiers());
    }

    /**
     * {@inheritDoc}
     *
     * @return true if field is declared public, false otherwise
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

  /**
   * A predicate that tests for visibility of a class, method, constructor, or field relative to a
   * particular package.
   *
   * <ul>
   *   <li>A top-level class is accessible from the package if it is public, or if it is
   *       package-private in the given package.
   *   <li>An element is accessible from the package if it is either public, or, if in the same
   *       package, then not private.
   * </ul>
   *
   * <p>The predicate is used to determine what can be accessed from a Randoop-generated JUnit test
   * in the given package. So, this class does not implement Java's full accessibility rules; those
   * for subclasses and default-visibility are not relevant to this predicate.
   */
  public static class PackageVisibilityPredicate extends VisibilityPredicate {

    /** The package name from which to test visibility of elements. */
    private final String packageName;

    /**
     * Create a predicate that tests visibility. Class members must either be public, or accessible
     * relative to the given package {@code packageName}.
     *
     * @param packageName the package to use for package accessibility test
     */
    public PackageVisibilityPredicate(String packageName) {
      this.packageName = packageName;
    }

    /**
     * {@inheritDoc}
     *
     * @return true if class is public or package private in {@code packageName}, false otherwise
     */
    @Override
    public boolean isVisible(Class<?> c) {
      int mods = c.getModifiers() & Modifier.classModifiers();
      return (c.getDeclaringClass() == null || isVisible(c.getDeclaringClass()))
          && isVisible(mods, c.getPackage());
    }

    /**
     * {@inheritDoc}
     *
     * @return true if method is public or a member of a class in {@code packageName} and not
     *     private, false otherwise
     */
    @Override
    public boolean isVisible(Method m) {
      int mods = m.getModifiers() & Modifier.methodModifiers();
      return isVisible(mods, m.getDeclaringClass().getPackage());
    }

    /**
     * {@inheritDoc}
     *
     * @return true if constructor is public or member of a class in {@code packageName} and not
     *     private, false otherwise
     */
    @Override
    public boolean isVisible(Constructor<?> c) {
      int mods = c.getModifiers() & Modifier.constructorModifiers();
      return isVisible(mods, c.getDeclaringClass().getPackage());
    }

    /**
     * {@inheritDoc}
     *
     * @return true if field is public or member of a class in {@code packageName} and not private,
     *     false otherwise
     */
    @Override
    public boolean isVisible(Field f) {
      int mods = f.getModifiers() & Modifier.fieldModifiers();
      return isVisible(mods, f.getDeclaringClass().getPackage());
    }

    /**
     * Test accessibility as indicated by the modifier bit string and/or package.
     *
     * @param mods the modifier bit string
     * @param otherPackage the package to test for relative accessibility
     * @return true if public set in modifiers or if otherPackage is the same as packageName and
     *     private is not set in modifiers, false otherwise
     */
    private boolean isVisible(int mods, Package otherPackage) {
      String otherPackageName = (otherPackage == null) ? "" : otherPackage.getName();
      return Modifier.isPublic(mods)
          || (packageName.equals(otherPackageName) && !Modifier.isPrivate(mods));
    }
  }

  /**
   * NotPrivateVisibilityPredicate is a {@link VisibilityPredicate} that returns true in the case
   * that the class/method/constructor/field is not declared to be private.
   */
  public static class NotPrivateVisibilityPredicate extends VisibilityPredicate {

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
}
