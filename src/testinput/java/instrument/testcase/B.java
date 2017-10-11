package instrument.testcase;

public class B {
  private int value;
  private A a;

  public B(int value) {
    this.value = value;
    a = new A(this);
  }

  public int getValue() {
    return value;
  }

  public int jumpValue() {
    return this.getValue() * a.getValue();
  }
}
