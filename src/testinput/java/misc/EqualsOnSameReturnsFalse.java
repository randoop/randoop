package misc;

public class EqualsOnSameReturnsFalse {
  public EqualsOnSameReturnsFalse() {
    /* No code. */
  }

  @Override
  public boolean equals(Object o) {
    return false;
  }

  @Override
  public String toString() {
    return "EqualsOnSameReturnsFalse";
  }
}
