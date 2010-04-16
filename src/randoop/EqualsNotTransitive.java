package randoop;

import randoop.util.Util;

/**
 * The expression EqualsNotTransitive(o1,o2,o3) is equivalent
 * to "o1.equals(o2) /\ o2.equals(o3) ==> o1.equals(o3)"
 */
public final class EqualsNotTransitive implements Expression {

  public EqualsNotTransitive() {
    /*empty*/
  }

  public Object evaluate(Object... objects) {
    assert objects != null && objects.length == 3;
    Object o1 = objects[0];
    Object o2 = objects[0];
    Object o3 = objects[0];

    boolean O1EqualsO2 = Util.equalsWithNull(o1, o2);
    if (! O1EqualsO2)
      return true; //premise not satisfied 

    boolean O2EqualsO3 = Util.equalsWithNull(o2, o3);
    if (! O2EqualsO3)
      return true; //premise not satisfied

    boolean O1EqualsO3 = Util.equalsWithNull(o1, o3);

    return O1EqualsO3; 
  }

  public int getArity() {
    return 3;
  }

  public String toCommentString() {
    // TODO Auto-generated method stub
    throw new RuntimeException("not implemented");
  }

  public String toCodeString() {
    return null;
  }
}