package randoop.contract;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import randoop.types.Type;
import randoop.types.TypeTuple;

/**
 * A check for a particular value of an <code>Enum</code>. To be used in regression tests when
 * <code>Enum</code> values are created.
 */
public final class EnumValue implements ObjectContract {

  public final Enum<?> value;
  private final Type type;

  public EnumValue(Enum<?> value) {
    this.value = value;
    this.type = Type.forClass(value.getDeclaringClass());
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof EnumValue)) {
      return false;
    }
    EnumValue enumValue = (EnumValue) obj;
    return value.equals(enumValue.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }

  @Override
  public String toString() {
    return "randoop.contract.EnumValue = " + value.name();
  }

  @Override
  public int getArity() {
    return 1;
  }

  @Override
  public TypeTuple getInputTypes() {
    List<Type> inputTypes = new ArrayList<>();
    inputTypes.add(type);
    return new TypeTuple(inputTypes);
  }

  @Override
  public boolean evaluate(Object... objects) throws Throwable {
    assert objects.length == 1;
    return value.equals(objects[0]);
  }

  @Override
  public boolean evalExceptionMeansFailure() {
    return true;
  }

  @Override
  public String toCommentString() {
    return null;
  }

  public String getValueName() {
    return type.getName() + "." + value.name();
  }

  @Override
  public String toCodeString() {
    String valueName = getValueName();
    StringBuilder b = new StringBuilder();
    b.append("org.junit.Assert.assertTrue(");
    b.append("\"'\" + " + "x0" + " + \"' != '\" + ").append(valueName).append(" + \"'\", ");
    b.append("x0");
    b.append(".equals(");
    b.append(valueName);
    b.append(")");
    b.append(");");
    return b.toString();
  }

  @Override
  public String get_observer_str() {
    return "EnumValue";
  }
}
