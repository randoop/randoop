package randoop.test;

import java.util.function.Predicate;
import randoop.sequence.ExecutableSequence;

/**
 * {@code RegressionTestPredicate} determines whether to keep an {@code ExecutableSequence} as a
 * regression test.
 */
public class RegressionTestPredicate implements Predicate<ExecutableSequence> {

  /**
   * Returns true if an executable sequence is a valid regression test. In particular, shouldn't
   * have failures (an error-revealing test), and shouldn't throw TimeoutException.
   *
   * <p>A true result means the test is a candidate for output.
   *
   * @return true if has no failures and does not involve a timeout exception, false otherwise
   */
  @Override
  public boolean test(ExecutableSequence eseq) {
    if (eseq.hasInvalidBehavior() || eseq.hasFailure()) {
      return false;
    }

    return true;
  }
}
