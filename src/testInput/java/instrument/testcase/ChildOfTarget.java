package instrument.testcase;

public class ChildOfTarget extends AbstractTarget {

  public ChildOfTarget(int i) {
    super(i);
  }

  @Override
  public void set(int i) {
    this.i = i;
  }
}
