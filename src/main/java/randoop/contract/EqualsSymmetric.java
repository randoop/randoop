package randoop.contract;

import java.util.ArrayList;
import java.util.List;
import randoop.Globals;
import randoop.types.JavaTypes;
import randoop.types.Type;
import randoop.types.TypeTuple;

/** The contract: <code>o1.equals(o2) &rArr; o2.equals(o1)</code>. */
public final class EqualsSymmetric implements ObjectContract {
  private static final EqualsSymmetric instance = new EqualsSymmetric();

  private EqualsSymmetric() {}

  public static EqualsSymmetric getInstance() {
    return instance;
  }

  @Override
  public boolean evaluate(Object... objects) {

    Object o1 = objects[0];
    Object o2 = objects[1];

    return !o1.equals(o2) || o2.equals(o1);
  }

  @Override
  public int getArity() {
    return 2;
  }

  @Override
  public TypeTuple getInputTypes() {
    List<Type> inputTypes = new ArrayList<>();
    inputTypes.add(JavaTypes.OBJECT_TYPE);
    inputTypes.add(JavaTypes.OBJECT_TYPE);
    return new TypeTuple(inputTypes);
  }

  @Override
  public String toCommentString() {
    return "equals-symmetric on x0 and x1.";
  }

  @Override
  public String get_observer_str() {
    return "equals-symmetric";
  }

  @Override
  public boolean evalExceptionMeansFailure() {
    return true;
  }

  @Override
  public String toCodeString() {
    StringBuilder b = new StringBuilder();
    b.append(Globals.lineSep);
    b.append("// This assertion (symmetry of equals) fails ");
    b.append(Globals.lineSep);
    b.append("org.junit.Assert.assertTrue(");
    b.append("\"Contract failed: " + toCommentString() + "\", ");
    b.append("x0.equals(x1) == x1.equals(x0)");
    b.append(");");
    return b.toString();
  }
}
