package randoop.contract;

import java.util.Arrays;
import randoop.types.JavaTypes;
import randoop.types.TypeTuple;

/** Checks that calling hashCode() on an object does not throw an exception. */
public final class HashCodeReturnsNormally extends ObjectContract {
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
      @SuppressWarnings("UnusedVariable") // Execute hashCode() but ignore its value.
      int ignore = o.hashCode();
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
    return "x0.hashCode() throws no Exception.";
  }

  @Override
  public String get_observer_str() {
    return "hashCode() throws no Exception";
  }

  @Override
  public String toCodeString() {
    StringBuilder b = new StringBuilder();
    b.append("org.junit.Assert.assertTrue(");
    b.append("\"Contract failed: " + toCommentString() + "\", ");
    b.append("x0.hashCode()");
    b.append(");");
    return b.toString();
  }
}
