package randoop.contract;

import java.util.ArrayList;
import java.util.List;

import randoop.Globals;
import randoop.types.ConcreteTypes;
import randoop.types.Type;
import randoop.types.TypeTuple;

/**
 * Checks that calling equals() on an object does not throw an exception.
 */
public final class EqualsReturnsNormally implements ObjectContract {
  private static final EqualsReturnsNormally instance = new EqualsReturnsNormally();

  private EqualsReturnsNormally() {}

  public static EqualsReturnsNormally getInstance() {
    return instance;
  }

  @Override
  public boolean evaluate(Object... objects) {
    assert objects != null && objects.length == 1;
    Object o = objects[0];
    assert o != null;
    try {
      //noinspection EqualsWithItself,ResultOfMethodCallIgnored
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

  @Override
  public TypeTuple getInputTypes() {
    List<Type> inputTypes = new ArrayList<>();
    inputTypes.add(ConcreteTypes.OBJECT_TYPE);
    return new TypeTuple(inputTypes);
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
    b.append("x0.equals()");
    b.append(");");
    return b.toString();
  }
}
