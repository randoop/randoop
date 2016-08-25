package randoop.contract;

import java.util.ArrayList;
import java.util.List;

import randoop.Globals;
import randoop.types.JavaTypes;
import randoop.types.Type;
import randoop.types.TypeTuple;

/**
 * The contract: <code>x0.equals(null)==false</code>.
 */
public final class EqualsToNullRetFalse implements ObjectContract {
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
    //noinspection ObjectEqualsNull
    return !o.equals(null);
  }

  @Override
  public int getArity() {
    return 1;
  }

  @Override
  public TypeTuple getInputTypes() {
    List<Type> inputTypes = new ArrayList<>();
    inputTypes.add(JavaTypes.OBJECT_TYPE);
    return new TypeTuple(inputTypes);
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
    b.append("!x0.equals(null)");
    b.append(");");
    return b.toString();
  }
}
