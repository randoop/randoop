package randoop.instrument.testcase;

public class C {
  private int value;
  public C (int value) {
    this.value = value;
  }
  public int getValue() { return value; }
  public int jumpValue() { return this.getValue() * this.getValue(); }
}
