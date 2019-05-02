package randoop.contract;

import java.util.Arrays;
import randoop.Globals;
import randoop.types.JavaTypes;
import randoop.types.TypeTuple;

/** The contract: {@code o1.equals(o2) ==> o1.hashCode() == o2.hashCode()}. */
public final class EqualsHashcode extends ObjectContract {
  private static final EqualsHashcode instance = new EqualsHashcode();

  private EqualsHashcode() {}

  public static EqualsHashcode getInstance() {
    return instance;
  }

  @Override
  public boolean evaluate(Object... objects) {

    Object o1 = objects[0];
    Object o2 = objects[1];

    return !o1.equals(o2) || o1.hashCode() == o2.hashCode();
  }

  @Override
  public int getArity() {
    return 2;
  }

  /** The arguments to which this contract can be applied. */
  static TypeTuple inputTypes =
      new TypeTuple(Arrays.asList(JavaTypes.OBJECT_TYPE, JavaTypes.OBJECT_TYPE));

  @Override
  public TypeTuple getInputTypes() {
    return inputTypes;
  }

  @Override
  public String toCommentString() {
    return "equals-hashcode on x0 and x1";
  }

  @Override
  public String get_observer_str() {
    return "EqualsHashcode";
  }

  @Override
  public String toCodeString() {
    StringBuilder b = new StringBuilder();
    b.append(Globals.lineSep);
    b.append("// Checks the contract: ");
    b.append(" ").append(toCommentString()).append(Globals.lineSep);
    b.append("org.junit.Assert.assertTrue(");
    b.append("\"Contract failed: ").append(toCommentString()).append("\", ");
    b.append("x0.equals(x1) ? x0.hashCode() == x1.hashCode() : true");
    b.append(");");
    return b.toString();
  }
}
