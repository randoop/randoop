package randoop;


public class EqualsSymmetric implements ObjectContract {

  public Object evaluate(Object... objects) {

    Object o1 = objects[0];
    Object o2 = objects[1];

    if (o1.equals(o2)) {
      return o2.equals(o1);
    }
    return true;
  }

  public int getArity() {
    return 2;
  }

  public String toCodeString() {
    return "x0.equals(x1) ? x1.equals(x0) : true";
  }

  public String toCommentString() {
    return "equals-symmetric on x0 and x1.";
  }

}
