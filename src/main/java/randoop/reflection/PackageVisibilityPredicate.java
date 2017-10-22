package randoop.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * A predicate that tests for visibility of a class, method, constructor, or field relative to a
 * particular package. A top-level class is accessible from the package if it is public, or if it is
 * package-private in the given package. Otherwise, an element is accessible from the package if it
 * is either public, or, if in the same package, then not private.
 *
 * <p>The predicate is used to determine what can be accessed from a Randoop generated JUnit test in
 * the given package. So, this class does not implement Java's full accessibility rules; those for
 * subclasses and default-visibility are not relevant to this predicate.
 */
public class PackageVisibilityPredicate extends VisibilityPredicate {

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
   * @return true if method is public or a member of a class in {@code packageName} and not private,
   *     false otherwise
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
    String otherPackageName = "";
    if (otherPackage != null) {
      otherPackageName = otherPackage.getName();
    }
    return Modifier.isPublic(mods)
        || (packageName.equals(otherPackageName) && !Modifier.isPrivate(mods));
  }
}
