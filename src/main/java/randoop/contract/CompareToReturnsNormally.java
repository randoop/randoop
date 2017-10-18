package randoop.contract;

import java.util.ArrayList;
import java.util.List;
import randoop.Globals;
import randoop.types.JavaTypes;
import randoop.types.Type;
import randoop.types.TypeTuple;

/** Checks that calling compareTo() on an object does not throw an exception. */
public class CompareToReturnsNormally implements ObjectContract {
  private static final CompareToReturnsNormally instance = new CompareToReturnsNormally();

  private CompareToReturnsNormally() {}

  public static CompareToReturnsNormally getInstance() {
    return instance;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public boolean evaluate(Object... objects) {
    assert objects != null && objects.length == 1;
    Object o = objects[0];
    assert o != null;
    if (o instanceof Comparable) {
      try {
        ((Comparable) o).compareTo(o);
      } catch (Exception e) {
        // If exception is thrown
        return false;
      }
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
    inputTypes.add(JavaTypes.COMPARABLE_TYPE);
    return new TypeTuple(inputTypes);
  }

  @Override
  public String toCommentString() {
    return "x0.compareTo() throws no Exception.";
  }

  @Override
  public String get_observer_str() {
    return "compareTo throws no Exception";
  }

  @Override
  public String toCodeString() {
    StringBuilder b = new StringBuilder();
    b.append(Globals.lineSep);
    b.append("// Checks the contract: ");
    b.append(" " + toCommentString() + Globals.lineSep);
    b.append("org.junit.Assert.assertTrue(");
    b.append("\"Contract failed: " + toCommentString() + "\", ");
    b.append("x0.compareTo()");
    b.append(");");
    return b.toString();
  }
}
