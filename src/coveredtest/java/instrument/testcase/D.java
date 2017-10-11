package instrument.testcase;

import randoop.CheckRep;

public class D {
  private int value;

  public D(int value) {
    this.value = value;
  }

  @CheckRep
  public boolean isZero() {
    return value == 0;
  }

  public int getValue() {
    return value;
  }

  public int jumpValue() {
    return this.getValue() * this.getValue();
  }
}
