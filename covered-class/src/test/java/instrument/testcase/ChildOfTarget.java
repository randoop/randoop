package instrument.testcase;

public class ChildOfTarget extends instrument.testcase.AbstractTarget {

  public ChildOfTarget(int i) {
    super(i);
  }

  @Override
  public void set(int i) {
    this.i = i;
  }
}
