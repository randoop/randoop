package randoop.test.predicate;

import randoop.ExceptionalExecution;
import randoop.sequence.ExecutableSequence;

/**
 * An {@code ExceptionPredicate} that always returns false.
 * Used to indicate that no exceptions belong to a behavior type.
 */
public class AlwaysFalseExceptionPredicate implements ExceptionPredicate {

  @Override
  public boolean test(ExceptionalExecution exec, ExecutableSequence s) {
    return false;
  }

}
