package randoop.contract;

import java.util.ArrayList;
import java.util.List;

import randoop.Globals;
import randoop.types.ConcreteTypes;
import randoop.types.GeneralType;
import randoop.types.TypeTuple;

/**
 * The contract: Checks that an object is antisymmetric over compareTo
 * <code>Math.signum(x0.compareTo(x1)) == -Math.signum(x1.compareTo(x0))</code>.
 */
public class CompareToAntiSymmetric implements ObjectContract {
  private static final CompareToAntiSymmetric instance = new CompareToAntiSymmetric();

  private CompareToAntiSymmetric() {}

  public static CompareToAntiSymmetric getInstance() {
    return instance;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public boolean evaluate(Object... objects) {
    Object o1 = objects[0];
    Object o2 = objects[1];

    // If o1 and o2 are comparable objects, check that their signs are opposite of one another
    if (o1 instanceof Comparable && o2 instanceof Comparable) {
      Comparable compObj1 = (Comparable) o1;
      Comparable compObj2 = (Comparable) o2;

      return Math.signum(compObj1.compareTo(compObj2))
          == -Math.signum(compObj2.compareTo(compObj1));
    }
    // If the compare to operation can't be done, the statement is trivially true
    return true;
  }

  @Override
  public int getArity() {
    return 2;
  }

  @Override
  public TypeTuple getInputTypes() {
    List<GeneralType> inputTypes = new ArrayList<>();
    inputTypes.add(ConcreteTypes.COMPARABLE_TYPE);
    inputTypes.add(ConcreteTypes.COMPARABLE_TYPE);
    return new TypeTuple(inputTypes);
  }

  @Override
  public String toCommentString() {
    return "compareTo-anti-symmetric on x0 and x1";
  }

  @Override
  public String get_observer_str() {
    return "CompareToAntiSymmetric";
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
    b.append(" ").append(toCommentString()).append(Globals.lineSep);
    b.append("org.junit.Assert.assertTrue(");
    b.append("\"Contract failed: ").append(toCommentString()).append("\", ");
    b.append("Math.signum(x0.compareTo(x1)) == -Math.signum(x1.compareTo(x0))");
    b.append(");");
    return b.toString();
  }
}
