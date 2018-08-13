package randoop.test;

import java.util.function.Predicate;
import randoop.sequence.ExecutableSequence;

public class ErrorTestPredicate implements Predicate<ExecutableSequence> {

  @Override
  public boolean test(ExecutableSequence eseq) {
    return eseq.hasFailure();
  }
}
