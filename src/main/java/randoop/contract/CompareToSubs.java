package randoop.contract;

import java.util.ArrayList;
import java.util.List;
import randoop.Globals;
import randoop.types.JavaTypes;
import randoop.types.Type;
import randoop.types.TypeTuple;

/**
 * The contract: Checks the substitutability property of compareTo for equals.
 *
 * <pre>(x0.compareTo(x1) == 0)
 * &rarr; (Math.signum(x0.compareTo(x2)) == Math.signum(x1.compareTo(x2)))</pre>
 */
public class CompareToSubs implements ObjectContract {
  private static final CompareToSubs instance = new CompareToSubs();

  private CompareToSubs() {}

  public static CompareToSubs getInstance() {
    return instance;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public boolean evaluate(Object... objects) {
    Object o1 = objects[0];
    Object o2 = objects[1];
    Object o3 = objects[2];

    // If o1 and o2 are comparable objects, check the implication
    if (o1 instanceof Comparable && o2 instanceof Comparable) {
      Comparable compObj1 = (Comparable) o1;
      Comparable compObj2 = (Comparable) o2;
      Comparable compObj3 = (Comparable) o3;

      return compObj1.compareTo(compObj2) != 0
          || Math.signum(compObj1.compareTo(compObj3)) == Math.signum(compObj2.compareTo(compObj3));
    }
    // If the compare to operation can't be done, the statement is trivially true
    return true;
  }

  @Override
  public int getArity() {
    return 3;
  }

  @Override
  public TypeTuple getInputTypes() {
    List<Type> inputTypes = new ArrayList<>();
    inputTypes.add(JavaTypes.COMPARABLE_TYPE);
    inputTypes.add(JavaTypes.COMPARABLE_TYPE);
    inputTypes.add(JavaTypes.COMPARABLE_TYPE);
    return new TypeTuple(inputTypes);
  }

  @Override
  public String toCommentString() {
    return "compareTo-substitutability on x0, x1, and x2";
  }

  @Override
  public String get_observer_str() {
    return "CompareToSubstitutability";
  }

  @Override
  public String toCodeString() {
    StringBuilder b = new StringBuilder();
    b.append(Globals.lineSep);
    b.append("// Checks the contract: ");
    b.append(" " + toCommentString() + Globals.lineSep);
    b.append("org.junit.Assert.assertTrue(");
    b.append("\"Contract failed: " + toCommentString() + "\", ");
    b.append(
        "!(x0.compareTo(x1) == 0) || (Math.signum(x0.compareTo(x2)) == Math.signum(x1.compareTo(x2)))");
    b.append(");");
    return b.toString();
  }
}
