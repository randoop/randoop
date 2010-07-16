package randoop.plugin.tests.ui.launching;

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
