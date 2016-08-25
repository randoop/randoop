package randoop.contract;

import java.util.ArrayList;
import java.util.List;

import randoop.Globals;
import randoop.types.JavaTypes;
import randoop.types.Type;
import randoop.types.TypeTuple;

/**
 * Checks that calling hashCode() on an object does not throw an exception.
 */
public final class HashCodeReturnsNormally implements ObjectContract {
  private static final HashCodeReturnsNormally instance = new HashCodeReturnsNormally();

  private HashCodeReturnsNormally() {}

  public static HashCodeReturnsNormally getInstance() {
    return instance;
  }

  @Override
  public boolean evaluate(Object... objects) {
    assert objects != null && objects.length == 1;
    Object o = objects[0];
    assert o != null;
    try {
      //noinspection ResultOfMethodCallIgnored
      o.hashCode();
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
    inputTypes.add(JavaTypes.OBJECT_TYPE);
    return new TypeTuple(inputTypes);
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
