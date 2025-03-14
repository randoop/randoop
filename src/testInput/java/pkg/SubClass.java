package pkg;

public class SubClass extends SuperClass {
  /**
  * @param count the count of things, must be positive
  */
  @Override
  public void methodWithOverride(int count) {
    super.methodWithOverride(count);
  }

  public void methodWithImplicitOverride(int count) {}
}
