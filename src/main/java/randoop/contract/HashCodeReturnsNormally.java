package randoop.contract;

import randoop.Globals;

/**
 * Checks that calling hashCode() on an object does not throw an exception.
 */
public final class HashCodeReturnsNormally implements ObjectContract {
  private static final HashCodeReturnsNormally instance = new HashCodeReturnsNormally();

  private HashCodeReturnsNormally() {};

  public static HashCodeReturnsNormally getInstance() {
    return instance;
  }

  @Override
  public boolean evaluate(Object... objects) {
    assert objects != null && objects.length == 1;
    Object o = objects[0];
    assert o != null;
    o.hashCode();
    return true;
  }

  @Override
  public int getArity() {
    return 1;
  }

  @Override
  public String toCommentString() {
    return "x0.hashCode() throws no Exception.";
  }

  @Override
  public String get_observer_str() {
    return "hashCode() throws no Exception";
  }

  @Override
  public boolean evalExceptionMeansFailure() {
    return true;
  }

  @Override
  public String toCodeString() {
    StringBuilder b = new StringBuilder();
    b.append(Globals.lineSep);
    b.append("// Checks the contract: ");
    b.append(" " + toCommentString() + Globals.lineSep);
    b.append("org.junit.Assert.assertTrue(");
    b.append("\"Contract failed: " + toCommentString() + "\", ");
    b.append("x0.hashCode()");
    b.append(");");
    return b.toString();
  }
}
