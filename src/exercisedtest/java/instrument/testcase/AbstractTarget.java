package instrument.testcase;

public abstract class AbstractTarget {

  protected int i;

  AbstractTarget(int i) {
    this.i = i;
  }

  public int get() {
    return i;
  }

  public abstract void set(int i);
}
