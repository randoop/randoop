package randoop.contract;

import randoop.Globals;

/**
 * The contract:
 * <code>o1.equals(o2) &rArr; o1.hashCode() == o2.hashCode()</code>.
 */
public final class EqualsHashcode implements ObjectContract {
  private static final EqualsHashcode instance = new EqualsHashcode();

  private EqualsHashcode() {};

  public static EqualsHashcode getInstance() {
    return instance;
  }

  @Override
  public boolean evaluate(Object... objects) {

    Object o1 = objects[0];
    Object o2 = objects[1];

    if (o1.equals(o2)) {
      return o1.hashCode() == o2.hashCode();
    }
    return true;
  }

  @Override
  public int getArity() {
    return 2;
  }

  @Override
  public String toCommentString() {
    return "equals-hashcode on x0 and x1";
  }

  @Override
  public String get_observer_str() {
    return "EqualsHashcode";
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
    b.append(" ").append(toCommentString()).append(Globals.lineSep);
    b.append("org.junit.Assert.assertTrue(");
    b.append("\"Contract failed: ").append(toCommentString()).append("\", ");
    b.append("x0.equals(x1) ? x0.hashCode() == x1.hashCode() : true");
    b.append(");");
    return b.toString();
  }
}
