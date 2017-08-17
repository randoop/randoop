package input;

import java.util.Objects;

/**
 * Class that is just like SystemExitClass, but this one does not call System.exit, so the replacecall
 * mechanism doesn't need to do any replacements.
 */
public class NoExitClass {
  private int value;

  public NoExitClass() {
    value = 0;
  }

  public NoExitClass(int value) {
    this.value = value;
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof NoExitClass)) {
      return false;
    }
    NoExitClass other = (NoExitClass) object;
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
    return this.value == value;
  }
}
