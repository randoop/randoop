package randoop.contract;

import java.util.Arrays;
import randoop.Globals;
import randoop.types.JavaTypes;
import randoop.types.TypeTuple;

/** Checks that calling toString() on an object does not throw an exception. */
public final class ToStringReturnsNormally extends ObjectContract {

  @Override
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (o == this) {
      return true;
    }
    return o instanceof ToStringReturnsNormally;
  }

  @Override
  public int hashCode() {
    return 51; // no state to compare.
  }

  @Override
  public boolean evaluate(Object... objects) {
    assert objects != null && objects.length == 1;
    Object o = objects[0];
    assert o != null;
    // noinspection ResultOfMethodCallIgnored
    o.toString();
    return true;
  }

  @Override
  public int getArity() {
    return 1;
  }

  static TypeTuple inputTypes = new TypeTuple(Arrays.asList(JavaTypes.OBJECT_TYPE));

  @Override
  public TypeTuple getInputTypes() {
    return inputTypes;
  }

  @Override
  public String toCommentString() {
    return "x0.toString() throws no Exception.";
  }

  @Override
  public String get_observer_str() {
    return "toString throws no Exception";
  }

  @Override
  public String toCodeString() {
    StringBuilder b = new StringBuilder();
    b.append(Globals.lineSep);
    b.append("// Checks the contract: ");
    b.append(" " + toCommentString() + Globals.lineSep);
    b.append("org.junit.Assert.assertTrue(");
    b.append("\"Contract failed: " + toCommentString() + "\", ");
    b.append("x0.toString()");
    b.append(");");
    return b.toString();
  }
}
