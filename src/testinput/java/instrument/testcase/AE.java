package instrument.testcase;

public class AE {
  private int value;

  AE(BE b) {
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
