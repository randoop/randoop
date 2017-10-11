package instrument.testcase;

public class BE {
  private int value;
  private instrument.testcase.AE a;

  public BE(int value) {
    this.value = value;
    a = new instrument.testcase.AE(this);
  }

  public int getValue() {
    return value;
  }

  public int jumpValue() {
    return this.getValue() * a.getValue();
  }
}
