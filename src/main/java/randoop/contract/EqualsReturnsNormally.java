package randoop.contract;

import java.util.Arrays;
import randoop.Globals;
import randoop.types.JavaTypes;
import randoop.types.TypeTuple;

/** Checks that calling equals() on an object does not throw an exception. */
public final class EqualsReturnsNormally extends ObjectContract {
  private static final EqualsReturnsNormally instance = new EqualsReturnsNormally();

  private EqualsReturnsNormally() {}

  public static EqualsReturnsNormally getInstance() {
    return instance;
  }

  @SuppressWarnings("SelfEquals")
  @Override
  public boolean evaluate(Object... objects) {
    assert objects != null && objects.length == 1;
    Object o = objects[0];
    assert o != null;
    try {
      // noinspection EqualsWithItself,ResultOfMethodCallIgnored
      o.equals(o);
    } catch (Exception e) {
      return false;
    }
    return true;
  }

  @Override
  public int getArity() {
    return 1;
  }

  /** The arguments to which this contract can be applied. */
  static TypeTuple inputTypes = new TypeTuple(Arrays.asList(JavaTypes.OBJECT_TYPE));

  @Override
  public TypeTuple getInputTypes() {
    return inputTypes;
  }

  @Override
  public String toCommentString() {
    return "x0.equals() throws no Exception.";
  }

  @Override
  public String get_observer_str() {
    return "equals() throws no Exception";
  }

  @Override
  public String toCodeString() {
    StringBuilder b = new StringBuilder();
    b.append(Globals.lineSep);
    b.append("// Checks the contract: ");
    b.append(" " + toCommentString() + Globals.lineSep);
    b.append("org.junit.Assert.assertTrue(");
    b.append("\"Contract failed: " + toCommentString() + "\", ");
    b.append("x0.equals()");
    b.append(");");
    return b.toString();
  }
}
