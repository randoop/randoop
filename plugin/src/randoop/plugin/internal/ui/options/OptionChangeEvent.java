package randoop.plugin.internal.ui.options;

/**
 * A simple implementation of <code>IOptionChangeEvent</code>. Note that option
 * change events are constant.
 * 
 * @author Peter Kalauskas
 */
public class OptionChangeEvent implements IOptionChangeEvent {

  private String fAttribute;

  private Object fValue;

  /**
   * Constructs a new option change event.
   * 
   * @param attribute
   * @param value
   */
  public OptionChangeEvent(String attribute, Object value) {
    fAttribute = attribute;
    fValue = value;
  }

  public Object getValue() {
    return fValue;
  }

  public String getAttributeName() {
    return fAttribute;
  }

}
