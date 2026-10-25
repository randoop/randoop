package instrument.testcase;

public class A {
  private int value;

  A(instrument.testcase.B b) {
    this.value = b.getValue();
  }

  public int getValue() {
    return value;
  }

  @Override
  public String toString() {
    return "a(" + value + ")";
  }
}
