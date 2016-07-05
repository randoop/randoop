package misc;

/**
 * This class corresponds to issue reported by Matias Martinez where a method throws an anonymous
 * exception and getCanonicalName() returns null.
 */
public class ThrowsAnonymousException {
  public ThrowsAnonymousException() {}

  public void trouble(int i) throws Exception {
    if (i < 0 || i > 0) {
      throw new Exception() {
        public void test() {};
      };
    }
  }
}
