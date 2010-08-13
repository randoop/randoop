package randoop.plugin.internal.ui.options;

public class OptionChangeEvent implements IOptionChangeEvent {
  String fAttribute;
  Object fValue;

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
