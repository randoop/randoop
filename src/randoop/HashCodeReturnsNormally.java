package randoop;

/**
 * Checks that calling hashCode() on a method does not throw an exception.
 */
public class HashCodeReturnsNormally implements ObjectContract {

  public Object evaluate(Object... objects) {
    assert objects != null && objects.length == 1;
    Object o = objects[0];
    assert o != null;
    o.hashCode();
    return true;
  }

  public int getArity() {
    return 1;
  }

  public String toCommentString() {
    return "x0.hashCode() throws no Exception.";
  }

  public String toCodeString() {
    return "x0.hashCode()";
  }

}
