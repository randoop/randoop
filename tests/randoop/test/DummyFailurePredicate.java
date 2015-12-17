package randoop.test;

import randoop.ExceptionalExecution;
import randoop.sequence.ExecutableSequence;

//Dummy failure predicate -- everything fails! --- for tests
public class DummyFailurePredicate implements FailureExceptionPredicate {

  @Override
  public boolean test(ExceptionalExecution exec, ExecutableSequence s) {
    return false;
  }

}
