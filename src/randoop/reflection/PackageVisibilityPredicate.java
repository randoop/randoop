package randoop.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * A predicate that tests for visibility of a class, method, constructor, or
 * field relative to a particular local package.
 * For classes, tests if the class is either public or package private in
 * the local package.
 * Otherwise, implements a test for Java package accessibility rules, and
 * returns true for a member that is visible from the context of the local 
 * package (either publicly accessible, or if in the same package then not private).
 */
public class PackageVisibilityPredicate implements VisibilityPredicate {

  private Package localPackage;

  /**
   * Create a predicate that tests visibility. Class members must either be
   * public, or in the given package {@code localPackage} and not private.
   * 
   * @param localPackage  the package to use for package access test.
   */
  public PackageVisibilityPredicate(Package localPackage) {
    this.localPackage = localPackage;
  }
  
  /**
   * {@inheritDoc}
   * @return true if class is public or package private in {@code localPackage}, false otherwise
   */
  @Override
  public boolean isVisible(Class<?> c) {
    int mods = c.getModifiers() & Modifier.classModifiers();
    return isVisible(mods, c.getPackage());
  }

  /**
   * {@inheritDoc}
   * @return true if method is public or a member of a class in 
   * {@code localPackage} and not private, false otherwise
   */
  @Override
  public boolean isVisible(Method m) {
    int mods = m.getModifiers() & Modifier.methodModifiers();
    return isVisible(mods, m.getDeclaringClass().getPackage());
  }

  /**
   * {@inheritDoc}
   * @return true if constructor is public or member of a class in 
   * {@code localPackage} and not private, false otherwise
   */
  @Override
  public boolean isVisible(Constructor<?> c) {
    int mods = c.getModifiers() & Modifier.constructorModifiers();
    return isVisible(mods, c.getDeclaringClass().getPackage());
  }

  /**
   * {@inheritDoc}
   * @return true if field is public or member of a class in 
   * {@code localPackage} and not private, false otherwise
   */
  @Override
  public boolean isVisible(Field f) {
    int mods = f.getModifiers() & Modifier.fieldModifiers();
    return isVisible(mods, f.getDeclaringClass().getPackage());
  }

  /**
   * Test visibility criteria on modifier bit string and package: either 
   * public bit of modifier is set, or packages are equal and the private bit
   * of modifier is not.
   * 
   * @param mods  the modifiers to test
   * @param otherPackage  the package to test for relative accessibility
   * @return true if public set in modifiers or if packages are the same and 
   * private is not set in modifiers, false otherwise
   */
  private boolean isVisible(int mods, Package otherPackage) {
    return Modifier.isPublic(mods)
        || (localPackage.equals(otherPackage) 
            && ! Modifier.isPrivate(mods));
  }

}
