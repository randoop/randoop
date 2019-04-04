package randoop.contract;

import java.util.Arrays;
import randoop.Globals;
import randoop.types.JavaTypes;
import randoop.types.TypeTuple;

/**
 * The contract: Checks the transitivity of the compare to method.
 *
 * <pre>{@code
 * ((x0.compareTo(x1) > 0) && (x1.compareTo(x2) > 0))
 *  => (x0.compareTo(x2) > 0)
 * }</pre>
 */
public class CompareToTransitive extends ObjectContract {
  private static final CompareToTransitive instance = new CompareToTransitive();

  private CompareToTransitive() {}

  public static CompareToTransitive getInstance() {
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

      return !(compObj1.compareTo(compObj2) > 0 && compObj2.compareTo(compObj3) > 0)
          || (compObj1.compareTo(compObj3) > 0);
    }
    // If the compare to operation can't be done, the statement is trivially true
    return true;
  }

  @Override
  public int getArity() {
    return 3;
  }

  static TypeTuple inputTypes =
      new TypeTuple(
          Arrays.asList(
              JavaTypes.COMPARABLE_TYPE, JavaTypes.COMPARABLE_TYPE, JavaTypes.COMPARABLE_TYPE));

  @Override
  public TypeTuple getInputTypes() {
    return inputTypes;
  }

  @Override
  public String toCommentString() {
    return "compareTo-transitive on x0, x1, and x2";
  }

  @Override
  public String get_observer_str() {
    return "CompareToTransitive";
  }

  @Override
  public String toCodeString() {
    StringBuilder b = new StringBuilder();
    b.append(Globals.lineSep);
    b.append("// Checks the contract: ");
    b.append(" " + toCommentString() + Globals.lineSep);
    b.append("org.junit.Assert.assertTrue(");
    b.append("\"Contract failed: " + toCommentString() + "\", ");
    b.append("!(x0.compareTo(x1)>0 && x1.compareTo(x2)>0) || x0.compareTo(x2)>0");
    b.append(");");
    return b.toString();
  }
}
