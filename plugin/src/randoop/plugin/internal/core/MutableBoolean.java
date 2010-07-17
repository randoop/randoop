package randoop.plugin.internal.core;

public class MutableBoolean {
  boolean fValue;

  public MutableBoolean(boolean value) {
    fValue = value;
  }

  public boolean getValue() {
    return fValue;
  }

  public void setValue(boolean value) {
    fValue = value;
  }
}
