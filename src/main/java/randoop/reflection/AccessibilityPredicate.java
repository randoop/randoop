package randoop.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/** Interface for predicates that check whether a class or class member is considered accessible. */
public abstract class AccessibilityPredicate {

  /** A predicate that returns true for public elements. */
  public static AccessibilityPredicate IS_PUBLIC = new PublicAccessibilityPredicate();
  /** A predicate that returns true for non-private elements. */
  public static AccessibilityPredicate IS_NOT_PRIVATE = new NotPrivateAccessibilityPredicate();
  /** A predicate that always returns true. */
  public static AccessibilityPredicate IS_ANY = new AnyAccessibilityPredicate();

  /**
   * Determines whether this AccessibilityPredicate considers a {@link Class} accessible.
   *
   * @param c the class object to check
   * @return whether this considers the class to be accessible
   */
  public abstract boolean isAccessible(Class<?> c);

  /**
   * Determines whether this AccessibilityPredicate considers a {@link Method} or {@link
   * Constructor} accessible. Does not test the accessibility of the containing class.
   *
   * @param e the method/constructor object to check
   * @return whether this considers the method/constructor to be accessible
   */
  public abstract boolean isAccessible(Executable e);

  /**
   * Determines whether this AccessibilityPredicate considers a {@link Field} accessible. Does not
   * test the accessibility of the containing class.
   *
   * @param f the field object to check
   * @return whether this considers the field to be accessible
   */
  public abstract boolean isAccessible(Field f);

  /** AnyAccessibilityPredicate is a {@link AccessibilityPredicate} that always returns true. */
  private static class AnyAccessibilityPredicate extends AccessibilityPredicate {

    /**
     * {@inheritDoc}
     *
     * @return true
     */
    @Override
    public boolean isAccessible(Class<?> c) {
      return true;
    }

    /**
     * {@inheritDoc}
     *
     * @return true
     */
    @Override
    public boolean isAccessible(Executable e) {
      return true;
    }

    /**
     * {@inheritDoc}
     *
     * @return true
     */
    @Override
    public boolean isAccessible(Field f) {
      return true;
    }

    @Override
    public String toString() {
      return "AnyAccessibilityPredicate";
    }
  }

  /**
   * PublicAccessibilityPredicate is a {@link AccessibilityPredicate} that returns true in the case
   * that the class/method/constructor/field is public.
   */
  private static class PublicAccessibilityPredicate extends AccessibilityPredicate {

    /**
     * {@inheritDoc}
     *
     * @return true if class is declared public, false otherwise
     */
    @Override
    public boolean isAccessible(Class<?> c) {
      return (c.getDeclaringClass() == null || isAccessible(c.getDeclaringClass()))
          && isAccessible(c.getModifiers() & Modifier.classModifiers());
    }

    /**
     * {@inheritDoc}
     *
     * @return true if method/constructor is declared public, false otherwise
     */
    @Override
    public boolean isAccessible(Executable e) {
      return isAccessible(e.getModifiers());
    }

    /**
     * {@inheritDoc}
     *
     * @return true if field is declared public, false otherwise
     */
    @Override
    public boolean isAccessible(Field f) {
      return isAccessible(f.getModifiers() & Modifier.fieldModifiers());
    }

    /*
     * Checks whether the provided modifiers indicate public bit is set.
     */
    private boolean isAccessible(int mods) {
      return Modifier.isPublic(mods);
    }

    @Override
    public String toString() {
      return "PublicAccessibilityPredicate";
    }
  }

  /**
   * A predicate that tests for accessibility of a class, method, constructor, or field relative to
   * a particular package.
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
   * for subclasses and default-accessibility are not relevant to this predicate.
   */
  public static class PackageAccessibilityPredicate extends AccessibilityPredicate {

    /** The package name from which to test accessibility of elements. */
    private final String packageName;

    /**
     * Create a predicate that tests accessibility. Class members must either be public, or
     * accessible relative to the given package {@code packageName}.
     *
     * @param packageName the package to use for package accessibility test
     */
    public PackageAccessibilityPredicate(String packageName) {
      this.packageName = packageName;
    }

    /**
     * {@inheritDoc}
     *
     * @return true if class is public or package private in {@code packageName}, false otherwise
     */
    @Override
    public boolean isAccessible(Class<?> c) {
      int mods = c.getModifiers() & Modifier.classModifiers();
      return (c.getDeclaringClass() == null || isAccessible(c.getDeclaringClass()))
          && isAccessible(mods, c.getPackage());
    }

    /**
     * {@inheritDoc}
     *
     * @return true if method/constructor is public or a member of a class in {@code packageName}
     *     and not private, false otherwise
     */
    @Override
    public boolean isAccessible(Executable e) {
      int mods = e.getModifiers();
      return isAccessible(mods, e.getDeclaringClass().getPackage());
    }

    /**
     * {@inheritDoc}
     *
     * @return true if field is public or member of a class in {@code packageName} and not private,
     *     false otherwise
     */
    @Override
    public boolean isAccessible(Field f) {
      int mods = f.getModifiers() & Modifier.fieldModifiers();
      return isAccessible(mods, f.getDeclaringClass().getPackage());
    }

    /**
     * Test accessibility as indicated by the modifier bit string and/or package.
     *
     * @param mods the modifier bit string
     * @param otherPackage the package to test for relative accessibility
     * @return true if public set in modifiers or if otherPackage is the same as packageName and
     *     private is not set in modifiers, false otherwise
     */
    private boolean isAccessible(int mods, Package otherPackage) {
      String otherPackageName = (otherPackage == null) ? "" : otherPackage.getName();
      return Modifier.isPublic(mods)
          || (packageName.equals(otherPackageName) && !Modifier.isPrivate(mods));
    }

    @Override
    public String toString() {
      return "PackageAccessibilityPredicate(" + packageName + ")";
    }
  }

  /**
   * NotPrivateAccessibilityPredicate is a {@link AccessibilityPredicate} that returns true in the
   * case that the class/method/constructor/field is not declared to be private.
   */
  private static class NotPrivateAccessibilityPredicate extends AccessibilityPredicate {

    /**
     * {@inheritDoc}
     *
     * @return true if the class access modifier is not private, and false, otherwise
     */
    @Override
    public boolean isAccessible(Class<?> c) {
      return (c.getDeclaringClass() == null || isAccessible(c.getDeclaringClass()))
          && isAccessible(c.getModifiers() & Modifier.classModifiers());
    }

    /**
     * {@inheritDoc}
     *
     * @return true if the method/constructor access modifier is not private, and false, otherwise
     */
    @Override
    public boolean isAccessible(Executable e) {
      return isAccessible(e.getModifiers());
    }

    /**
     * {@inheritDoc}
     *
     * @return true if the field access modifier is not private, and false, otherwise
     */
    @Override
    public boolean isAccessible(Field f) {
      return isAccessible(f.getModifiers() & Modifier.fieldModifiers());
    }

    /**
     * Returns true if the {@link java.lang.reflect.Modifier Modifier} value does not have private
     * set.
     *
     * @param mods the modifiers value
     * @return true if the private bit is not set, false otherwise
     */
    private boolean isAccessible(int mods) {
      return !Modifier.isPrivate(mods);
    }

    @Override
    public String toString() {
      return "NotPrivateAccessibilityPredicate";
    }
  }
}
