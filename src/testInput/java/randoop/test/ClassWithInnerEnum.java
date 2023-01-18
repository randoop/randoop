package randoop.test;

/** Input class for enum testing. */
public class ClassWithInnerEnum {
  public Toggle toggleSwitch;
  public OtherToggle otherToggle;

  public ClassWithInnerEnum() {
    toggleSwitch = Toggle.OFF;
    otherToggle = OtherToggle.ON;
  }

  public enum Toggle {
    OFF,
    ON;

    public boolean isOn() {
      return this != OFF;
    }
  }

  public enum OtherToggle {
    OFF {
      @Override
      public boolean isOn() {
        return false;
      }
    },
    ON {
      @Override
      public boolean isOn() {
        return true;
      }
    };

    public abstract boolean isOn();
  }

  public Toggle getToggleSwitch() {
    return toggleSwitch;
  }

  public OtherToggle getOtherToggleSwitch() {
    return otherToggle;
  }
}
