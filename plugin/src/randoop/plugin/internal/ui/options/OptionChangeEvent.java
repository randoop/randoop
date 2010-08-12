package randoop.plugin.internal.ui.options;

public class OptionChangeEvent implements IOptionChangeEvent {
  String fAttribute;
  String fValue;

  public OptionChangeEvent(String attribute, String value) {
    fAttribute = attribute;
    fValue = value;
  }

  public String getValue() {
    return fValue;
  }

  public String getAttribute() {
    return fAttribute;
  }
}
