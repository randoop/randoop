package randoop.plugin.internal.core;

/**
 * Simple mutable-<code>boolean</code> implementation. This is a convenience class
 * that can be declared <code>final</code> for use inside anonymous inner types.
 * 
 * @author Peter Kalauskas
 */
public class MutableBoolean {
  private boolean fValue;

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
