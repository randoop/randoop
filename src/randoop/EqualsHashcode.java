package randoop;

public class EqualsHashcode implements ObjectContract {

  public EqualsHashcode() {
    // Empty body.
  }

  public Object evaluate(Object... objects) {

    Object o1 = objects[0];
    Object o2 = objects[1];

    if (o1.equals(o2)) {
      return o1.hashCode() == o2.hashCode();
    }
    return true;
  }

  public int getArity() {
    return 2;
  }

  public String toCodeString() {
    return "x0.equals(x1) ? x0.hashCode() == x1.hashCode() : true";
  }

  public String toCommentString() {
    return "equals-hashcode on x0 and x1";
  }

}
