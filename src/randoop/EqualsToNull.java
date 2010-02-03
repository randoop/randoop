package randoop;

/**
 * The expression "!x0.equals(null)"
 */
public final class EqualsToNull implements ObjectContract {

  public EqualsToNull() {
    /*empty*/
  }

  public Object evaluate(Object... objects) {
    assert objects != null && objects.length == 1;
    Object o = objects[0];
    assert o != null;
    return !o.equals(null);
  }

  public int getArity() {
    return 1;
  }

  public String toCommentString() {
    return "!x0.equals(null)";
  }

  public String toCodeString() {
    return "!x0.equals(null)";
  }
}