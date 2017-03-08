package pkg;

public class SuperClass {
  /**
   * @param count  the object count, must be positive
   */
  public void methodWithOverride(int count) { }

/**
 * @param count  the object count, must be positive
 */
  public void methodWithImplicitOverride(int count) { }

  /**
  * @param count  the something count, must be positive
  */
  public void methodWithoutOverride(int count) {
    if (count <= 0) System.out.println("without-override: " + count);
  }
}
