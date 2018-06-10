package randoop.util;

/** Indicates that a test execution timed out. */
public final class TimeoutExceededException extends RuntimeException {

  private static final long serialVersionUID = 7932531804127083492L;

  public TimeoutExceededException() {}

  public TimeoutExceededException(String string) {
    super(string);
  }
}
