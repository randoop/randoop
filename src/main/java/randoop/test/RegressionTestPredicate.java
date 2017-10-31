package randoop.test;

import randoop.sequence.ExecutableSequence;
import randoop.util.predicate.DefaultPredicate;

/**
 * {@code RegressionTestPredicate} determines whether to keep an {@code ExecutableSequence} as a
 * regression test.
 */
public class RegressionTestPredicate extends DefaultPredicate<ExecutableSequence> {

  /**
   * Determines whether an executable sequence is a valid regression test. In particular, shouldn't
   * have failures (an error-revealing test), and shouldn't have {@link
   * randoop.util.TimeoutExceededException TimeoutExceededException}.
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
