package randoop.plugin.internal.core;

/**
 * Simple mutable-<code>Object</code> implementation. This is a convenience class
 * that can be declared <code>final</code> for use inside anonymous inner types.
 * 
 * @author Peter Kalauskas
 */
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
