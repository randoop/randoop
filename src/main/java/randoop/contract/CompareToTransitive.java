package randoop.contract;

import java.util.ArrayList;
import java.util.List;

import randoop.Globals;
import randoop.types.ConcreteTypes;
import randoop.types.GeneralType;
import randoop.types.TypeTuple;

/**
 * The contract: Checks the transitivity of the compare to method
 * <code>((x0.compareTo(x1) &gt; 0) &amp;&amp; (x1.compareTo(x2) &gt; 0)) &rarr; (x0.compareTo(x2) &gt; 0)</code>.
 */
public class CompareToTransitive implements ObjectContract {
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

  @Override
  public TypeTuple getInputTypes() {
    List<GeneralType> inputTypes = new ArrayList<>();
    inputTypes.add(ConcreteTypes.COMPARABLE_TYPE);
    inputTypes.add(ConcreteTypes.COMPARABLE_TYPE);
    inputTypes.add(ConcreteTypes.COMPARABLE_TYPE);
    return new TypeTuple(inputTypes);
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
    b.append("!(x0.compareTo(x1)>0 && x1.compareTo(x2)>0) || x0.compareTo(x2)>0");
    b.append(");");
    return b.toString();
  }
}
