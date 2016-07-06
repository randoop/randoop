package randoop.contract;

import java.util.ArrayList;
import java.util.List;

import randoop.Globals;
import randoop.types.ConcreteTypes;
import randoop.types.GeneralType;
import randoop.types.TypeTuple;

/**
 * The contract: Checks the transitivity of equals for an object
 * <code>(x0.equals(x1) &amp;&amp; x1.equals(x2)) &rarr; x0.equals(x2)</code>.
 */
public class EqualsTransitive implements ObjectContract {
  private static final EqualsTransitive instance = new EqualsTransitive();

  private EqualsTransitive() {}

  public static EqualsTransitive getInstance() {
    return instance;
  }

  @Override
  public boolean evaluate(Object... objects) {
    Object o1 = objects[0];
    Object o2 = objects[1];
    Object o3 = objects[2];

    return !(o1.equals(o2) && (o2.equals(o3))) || o1.equals(o3);
  }

  @Override
  public int getArity() {
    return 3;
  }

  @Override
  public TypeTuple getInputTypes() {
    List<GeneralType> inputTypes = new ArrayList<>();
    inputTypes.add(ConcreteTypes.OBJECT_TYPE);
    inputTypes.add(ConcreteTypes.OBJECT_TYPE);
    inputTypes.add(ConcreteTypes.OBJECT_TYPE);
    return new TypeTuple(inputTypes);
  }

  @Override
  public String toCommentString() {
    return "equals-transitive on x0, x1, and x2.";
  }

  @Override
  public String get_observer_str() {
    return "equals-transitive";
  }

  @Override
  public boolean evalExceptionMeansFailure() {
    return true;
  }

  @Override
  public String toCodeString() {
    StringBuilder b = new StringBuilder();
    b.append(Globals.lineSep);
    b.append("// This assertion (transitivity of equals) fails ");
    b.append(Globals.lineSep);
    b.append("org.junit.Assert.assertTrue(");
    b.append("\"Contract failed: " + toCommentString() + "\", ");
    b.append("!(x0.equals(x1) && x1.equals(x2)) || x0.equals(x2)");
    b.append(");");
    return b.toString();
  }
}
