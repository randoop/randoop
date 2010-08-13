package randoop;



/**
 * The contract: <code>x != null</code>.
 * <p>
 * Obviously, this is not a property that must hold of all objects in a test.
 * Randoop creates an instance of this contract when, during execution of
 * a sequence, it determines that the above property holds. The property
 * thus represents a <i>regression</i> as it captures the behavior of the
 * code when it is executed.
 */
public final class IsNotNull implements ObjectContract {

  private static final long serialVersionUID = 20100429; 

  @Override
  public boolean equals(Object o) {
    if (o == null)
      return false;
    if (o == this)
      return true;
    if (!(o instanceof IsNotNull)) {
      return false;
    }
    return true; // no state to compare.
  }

  @Override
  public int hashCode() {
    int h = 31;
    return h;  // no state to compare.
  }
  
  
  @Override
  public boolean evaluate(Object... objects) throws Throwable {
    assert objects.length == 1;
    return objects[0] != null;
  }

  @Override
  public int getArity() {
    return 1;
  }

  @Override
  public String toCodeString() {
    StringBuilder b = new StringBuilder();
    b.append(Globals.lineSep);
    b.append("// Regression assertion (captures the current behavior of the code)" + Globals.lineSep);
    b.append("assertNotNull(x0);");
    return b.toString();
  }

  @Override
  public String toCommentString() {
    return "x0 != null";
  }

  @Override
  public String get_observer_str() {
    return "IsNotNull";
  }

  @Override
  public boolean evalExceptionMeansFailure() {
    // Should never actually reach here: no way evaluating "objects[0] != null"
    // will throw an exception.
   return false; 
  }

}
