package randoop.contract;

import java.util.Arrays;
import randoop.Globals;
import randoop.types.JavaTypes;
import randoop.types.TypeTuple;

/**
 * The contract: Checks that an object is reflexive over compareTo. {@code x0.compareTo(x0) == 0}.
 */
public class CompareToReflexive extends ObjectContract {
  private static final CompareToReflexive instance = new CompareToReflexive();

  private CompareToReflexive() {}

  public static CompareToReflexive getInstance() {
    return instance;
  }

  @SuppressWarnings({"unchecked", "rawtypes", "SelfComparison"})
  @Override
  public boolean evaluate(Object... objects) {
    assert objects != null && objects.length == 1;
    // Get first and only object
    Object o1 = objects[0];
    assert o1 != null;

    if (o1 instanceof Comparable) {
      Comparable compObj1 = (Comparable) o1;
      return (compObj1.compareTo(compObj1) == 0);
    }
    return true;
  }

  @Override
  public int getArity() {
    return 1;
  }

  /** The arguments to which this contract can be applied. */
  static TypeTuple inputTypes = new TypeTuple(Arrays.asList(JavaTypes.COMPARABLE_TYPE));

  @Override
  public TypeTuple getInputTypes() {
    return inputTypes;
  }

  @Override
  public String toCommentString() {
    return "compareTo-reflexive on x0";
  }

  @Override
  public String get_observer_str() {
    return "CompareToReflexive";
  }

  @Override
  public String toCodeString() {
    StringBuilder b = new StringBuilder();
    b.append(Globals.lineSep);
    b.append("// Checks the contract: ");
    b.append(" " + toCommentString() + Globals.lineSep);
    b.append("org.junit.Assert.assertTrue(");
    b.append("\"Contract failed: " + toCommentString() + "\", ");
    b.append("x0.compareTo(x0) == 0");
    b.append(");");
    return b.toString();
  }
}
