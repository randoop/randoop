package randoop.contract;

import java.util.Arrays;
import randoop.Globals;
import randoop.types.JavaTypes;
import randoop.types.TypeTuple;

/**
 * The contract: Checks that compareTo is consistent with equals.
 *
 * <pre>(x0.compareTo(x1) == 0) == x0.equals(x1)</pre>
 */
public class CompareToEquals extends ObjectContract {
  private static final CompareToEquals instance = new CompareToEquals();

  private CompareToEquals() {}

  public static CompareToEquals getInstance() {
    return instance;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public boolean evaluate(Object... objects) {
    Object o1 = objects[0];
    Object o2 = objects[1];

    if (o1 instanceof Comparable) {
      Comparable compObj1 = (Comparable) o1;
      return (compObj1.compareTo(o2) == 0) == o1.equals(o2);
    }
    return true;
  }

  @Override
  public int getArity() {
    return 2;
  }

  /** The arguments to which this contract can be applied. */
  static TypeTuple inputTypes =
      new TypeTuple(Arrays.asList(JavaTypes.COMPARABLE_TYPE, JavaTypes.COMPARABLE_TYPE));

  @Override
  public TypeTuple getInputTypes() {
    return inputTypes;
  }

  @Override
  public String toCommentString() {
    return "compareTo-equals on x0 and x1";
  }

  @Override
  public String get_observer_str() {
    return "CompareToEquals";
  }

  @Override
  public String toCodeString() {
    StringBuilder b = new StringBuilder();
    b.append(Globals.lineSep);
    b.append("// Checks the contract: ");
    b.append(" " + toCommentString() + Globals.lineSep);
    b.append("org.junit.Assert.assertTrue(");
    b.append("\"Contract failed: " + toCommentString() + "\", ");
    b.append("(x0.compareTo(x1) == 0) == x0.equals(x1)");
    b.append(");");
    return b.toString();
  }
}
