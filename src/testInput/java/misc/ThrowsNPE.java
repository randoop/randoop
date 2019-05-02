package misc;

public class ThrowsNPE {
  public ThrowsNPE() {
    /* No code. */
  }
  // Throws an NPE.
  public void throwNPE() {
    throw new NullPointerException();
  }

  @Override
  public String toString() {
    return "ThrowsNPE";
  }
}
