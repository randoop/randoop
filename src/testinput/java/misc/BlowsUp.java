package misc;

public class BlowsUp {
  public BlowsUp() {
    /* No code. */
  }

  @Override
  public String toString() {
    throw new ToStringBlowsUp();
  }

  @Override
  public int hashCode() {
    throw new HashcodeBlowsUp();
  }

  @Override
  public boolean equals(Object o) {
    throw new EqualsBlowsUp();
  }

  public static class ToStringBlowsUp extends RuntimeException {
    private static final long serialVersionUID = 1L;
  }

  public static class HashcodeBlowsUp extends RuntimeException {
    private static final long serialVersionUID = 1L;
  }

  public static class EqualsBlowsUp extends RuntimeException {
    private static final long serialVersionUID = 1L;
  }
}
