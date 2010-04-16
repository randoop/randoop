package randoop;



/**
 * The fault-revealing behavior
 * 
 *    " ! (o1.equals(o2) <=> o2.equals(o1)) "
 *    
 * If any exceptional behavior occurs in one of the above
 * calls, the exceptional behavior is not exhibited.
 */
public final class EqualsNotSymmetric implements Expression {

  public EqualsNotSymmetric() {
    /*empty*/
  }

  public Object evaluate(Object... objects) {
    assert objects != null && objects.length == 2;
    Object o1 = objects[0];
    Object o2 = objects[1];
    return o1.equals(o2) == o2.equals(o1);
  }

  public int getArity() {
    return 2;
  }

  public String toCommentString() {
    // TODO Auto-generated method stub
    throw new RuntimeException("not implemented");
  }

  public String toCodeString() {
    return null;
  }
}
