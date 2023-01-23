package input;

import java.util.Objects;

/** Input class for testing that replacecall agent is handling {@code System.exit()} calls. */
public class SystemExitClass {
  private int value;

  public SystemExitClass() {
    value = 0;
  }

  public SystemExitClass(int value) {
    this.value = value;
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof SystemExitClass)) {
      return false;
    }
    SystemExitClass other = (SystemExitClass) object;
    return this.value == other.value;
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.value);
  }

  @Override
  public String toString() {
    return "" + value;
  }

  public int getValue() {
    return value;
  }

  /**
   * This method calls {@code System.exit(value)} if {@code value} is not equal to the value of this
   * object.
   *
   * @param value the value to check against the value of this object
   * @return true if {@code value} is the same as the value of this object, otherwise does not
   *     return
   */
  public boolean checkValue(int value) {
    if (this.value != value) {
      System.exit(value);
    }
    return true;
  }
}
