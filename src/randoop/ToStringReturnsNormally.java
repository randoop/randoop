package randoop;

/**
 * Checks that calling toString() on a method does not throw an exception.
 */
public class ToStringReturnsNormally implements ObjectContract {

  public Object evaluate(Object... objects) {
    assert objects != null && objects.length == 1;
    Object o = objects[0];
    assert o != null;
    o.toString();
    return true;
  }

  public int getArity() {
    return 1;
  }

  public String toCommentString() {
    return "x0.toString() throws no Exception.";
  }

  public String toCodeString() {
    return "x0.toString()";
  }

}
