package randoop;

/**
 * The expression "x0".
 */
public final class ValueExpression implements Expression {
  
  public ValueExpression() {
    // Empty body.
  }

  public Object evaluate(Object... objects) {
    assert objects != null && objects.length == 1;
    return objects[0];
  }

  public int getArity() {
    return 1;
  }

  public String toCommentString() {
    return "x0";
  }

  public String toCodeString() {
    return "x0";
  }
}
