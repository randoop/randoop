package randoop.test;

import randoop.sequence.ExecutableSequence;
import randoop.util.predicate.DefaultPredicate;

public class ErrorTestPredicate extends DefaultPredicate<ExecutableSequence> {

  @Override
  public boolean test(ExecutableSequence s) {
    return s.hasFailure();
  }

}
