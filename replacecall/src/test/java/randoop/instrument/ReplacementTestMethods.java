package randoop.instrument;

import java.util.Random;

/**
 * Methods that appear as replacements in the replacement files used by {@link ReplacementFileTest}.
 */
public class ReplacementTestMethods {

  /** Do not instantiate. */
  private ReplacementTestMethods() {
    throw new UnsupportedOperationException("Do not instantiate");
  }

  /**
   * A replacement for {@code Random.nextInt()} that is not public.
   *
   * @param r the receiver of the replaced call
   * @return 0
   */
  static int packagePrivateNextInt(Random r) {
    return 0;
  }

  /**
   * A replacement for {@code Random.nextInt()} that lacks the receiver parameter.
   *
   * @return 0
   */
  public static int nextIntWithoutReceiver() {
    return 0;
  }

  /**
   * A replacement for {@code Random.nextInt()} that has the wrong return type.
   *
   * @param r the receiver of the replaced call
   * @return 0
   */
  public static long nextIntReturningLong(Random r) {
    return 0;
  }

  /** A replacement class for {@code Random} whose replacement method is not public. */
  public static class PackagePrivateMethodClass {

    /** Do not instantiate. */
    private PackagePrivateMethodClass() {
      throw new UnsupportedOperationException("Do not instantiate");
    }

    /**
     * A replacement for {@code Random.nextInt()} that is not public.
     *
     * @param r the receiver of the replaced call
     * @return 0
     */
    static int nextInt(Random r) {
      return 0;
    }
  }

  /** An interface that declares a replacement method. */
  public interface ReplacementInterface {
    /**
     * A replacement for {@code Random.nextInt()} that is declared in an interface.
     *
     * @param r the receiver of the replaced call
     * @return 0
     */
    static int nextInt(Random r) {
      return 0;
    }
  }
}

/** A class that is not public. */
class NonPublicReplacementClass {

  /** Do not instantiate. */
  private NonPublicReplacementClass() {
    throw new UnsupportedOperationException("Do not instantiate");
  }

  /**
   * A replacement for {@code Random.nextInt()} that is declared in a non-public class.
   *
   * @param r the receiver of the replaced call
   * @return 0
   */
  public static int nextInt(Random r) {
    return 0;
  }
}
