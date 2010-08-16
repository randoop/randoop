package randoop.plugin.internal.core;

public class MutableObject {
  Object fValue;

  public MutableObject(Object value) {
    fValue = value;
  }

  public Object getValue() {
    return fValue;
  }

  public void setValue(Object value) {
    fValue = value;
  }

}
