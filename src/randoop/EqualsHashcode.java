package randoop;

/**
 * The contract: <code>o1.equals(o2) => o1.hashCode() == o2.hashCode()</code>.
 */
public final class EqualsHashcode implements ObjectContract {

  private static final long serialVersionUID = -1662539974264914487L;
  
  @Override
  public boolean equals(Object o) {
    if (o == null)
      return false;
    if (o == this)
      return true;
    if (!(o instanceof EqualsHashcode)) {
      return false;
    }
    return true; // no state to compare.
  }

  @Override
  public int hashCode() {
    int h = 7;
    return h;  // no state to compare.
  }

  public boolean evaluate(Object... objects) {

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

  public String toCommentString() {
    return "equals-hashcode on x0 and x1";
  }

  @Override
  public String get_observer_str() {
    return "EqualsHashcode";
  }

  public boolean evalExceptionMeansFailure() {
    return true;
  }

  @Override
  public String toCodeString() {
    StringBuilder b = new StringBuilder();
    b.append(Globals.lineSep);
    b.append("// Checks the contract: ");
    b.append(" " + toCommentString() + Globals.lineSep);
    b.append("assertTrue(");
    b.append("\"Contract failed: " + toCommentString() + "\", ");
    b.append("x0.equals(x1) ? x0.hashCode() == x1.hashCode() : true");
    b.append(");");
    return b.toString();
  }

}
