package randoop.contract;

import java.util.Arrays;
import java.util.Collection;
import randoop.Globals;
import randoop.types.JavaTypes;
import randoop.types.Type;
import randoop.types.TypeTuple;

/** The contract: {@code l.toArray().length == l.size()} for all Collections l. */
public final class SizeLength extends ObjectContract {
  private static final SizeLength instance = new SizeLength();

  private SizeLength() {}

  @Override
  public boolean evaluate(Object... objects) {
    assert objects != null && objects.length == 1;
    Object o = objects[0];
    if (o instanceof Collection) {
      Collection<?> c = (Collection<?>) o;
      assert c != null;
      return c.size() == c.toArray().length;
    }
    return true;
  }

  @Override
  public int getArity() {
    return 1;
  }

  static TypeTuple inputTypes = new TypeTuple(Arrays.<Type>asList(JavaTypes.COLLECTION_TYPE));

  @Override
  public TypeTuple getInputTypes() {
    return inputTypes;
  }

  @Override
  public String toCommentString() {
    return "x0.toArray().length == x0.size()";
  }

  @Override
  public String get_observer_str() {
    return "SizeLength";
  }

  @Override
  public String toCodeString() {
    StringBuilder b = new StringBuilder();
    b.append(Globals.lineSep);
    b.append("// Checks the contract: ");
    b.append(" " + toCommentString() + Globals.lineSep);
    b.append("org.junit.Assert.assertTrue(");
    b.append("\"Contract failed: " + toCommentString() + "\", ");
    b.append("x0.toArray().length == x0.size()");
    b.append(");");
    return b.toString();
  }
}
