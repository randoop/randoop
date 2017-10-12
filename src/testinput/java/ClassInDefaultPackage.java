// default package

/**
 * This is a class in the default package, and it should stay that way.
 *
 * <p>The class is intended to be used in tests that make sure that bad assumptions about package
 * names don't creep into Randoop, as they occasionally do.
 */
public class ClassInDefaultPackage {
  public final int number;

  public ClassInDefaultPackage(int number) {
    this.number = number;
  }

  public ClassInDefaultPackage add(ClassInDefaultPackage c) {
    return new ClassInDefaultPackage(this.number + c.number);
  }
}
