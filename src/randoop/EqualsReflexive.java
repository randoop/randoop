package randoop;


/**
 * The contract: <code>x0.equals(x0)==true</code>.
 */
public final class EqualsReflexive implements ObjectContract {

  private static final long serialVersionUID = 4769291376647737367L;

  @Override
  public boolean equals(Object o) {
    if (o == null)
      return false;
    if (o == this)
      return true;
    if (!(o instanceof EqualsReflexive)) {
      return false;
    }
    return true; // no state to compare.
  }

  @Override
  public int hashCode() {
    int h = 17;
    return h;  // no state to compare.
  }

  
  public boolean evaluate(Object... objects) {
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

  @Override
  public String get_observer_str() {
    return "EqualsReflexive";
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
    b.append("x0.equals(x0)");
    b.append(");");
    return b.toString();
  }
  
}
