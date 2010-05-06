package randoop;


/**
 * Represents the expression "x0.equals(x0)".
 */
public final class EqualsToItself implements ObjectContract {

  public EqualsToItself() {
    /*empty*/
  }

  public Object evaluate(Object... objects) {
    assert objects != null && objects.length == 1;
    Object o = objects[0];
    assert o != null;
    return o.equals(o);
  }

  public int getArity() {
    return 1;
  }

  public String toCommentString() {
    return "x0.equals(x0)";
  }

  public String toCodeString() {
    return "x0.equals(x0)";
  }
  
}
