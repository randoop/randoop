package input;

/** A replacement for {@code java.lang.String.hashCode()}, used by a replacecall system test. */
public final class StringHashCodeReplacement {

  /** Do not instantiate. */
  private StringHashCodeReplacement() {
    throw new UnsupportedOperationException("Do not instantiate");
  }

  /**
   * Replaces {@code s.hashCode()}.
   *
   * @param s a string
   * @return the hash code of {@code s}
   */
  public static int hashCode(String s) {
    // Does not call s.hashCode(), which the agent might replace by a call to this method.
    int h = 0;
    for (int i = 0; i < s.length(); i++) {
      h = 31 * h + s.charAt(i);
    }
    return h;
  }
}
