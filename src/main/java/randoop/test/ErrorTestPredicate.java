package randoop.test;

import java.util.function.Predicate;
import randoop.sequence.ExecutableSequence;

/** Returns true if the sequence is an error test (has a failure). */
public class ErrorTestPredicate implements Predicate<ExecutableSequence> {

  @Override
  public boolean test(ExecutableSequence eseq) {
    return eseq.hasFailure();
  }
}
