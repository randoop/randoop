package misc;

public class ThrowsRuntimeException {
  public ThrowsRuntimeException() {
    /* No code. */
  }

  public void throwRuntimeException() {
    throw new RuntimeException();
  }

  @Override
  public String toString() {
    return "ThrowsRuntimeException";
  }
}
