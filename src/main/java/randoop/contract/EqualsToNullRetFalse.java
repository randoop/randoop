package randoop.contract;

import java.util.Arrays;
import randoop.Globals;
import randoop.types.JavaTypes;
import randoop.types.TypeTuple;

/** The contract: {@code x0.equals(null)==false}. */
public final class EqualsToNullRetFalse extends ObjectContract {
  private static final EqualsToNullRetFalse instance = new EqualsToNullRetFalse();

  private EqualsToNullRetFalse() {}

  public static EqualsToNullRetFalse getInstance() {
    return instance;
  }

  @Override
  public boolean evaluate(Object... objects) {
    assert objects != null && objects.length == 1;
    Object o = objects[0];
    assert o != null;
    // noinspection ObjectEqualsNull
    return !o.equals(null);
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
    return "!x0.equals(null)";
  }

  @Override
  public String get_observer_str() {
    return "equalsNull @";
  }

  @Override
  public String toCodeString() {
    StringBuilder b = new StringBuilder();
    b.append(Globals.lineSep);
    b.append("// Checks the contract: ");
    b.append(" " + toCommentString() + Globals.lineSep);
    b.append("org.junit.Assert.assertTrue(");
    b.append("\"Contract failed: " + toCommentString() + "\", ");
    b.append("!x0.equals(null)");
    b.append(");");
    return b.toString();
  }
}
