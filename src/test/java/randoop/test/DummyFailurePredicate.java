package randoop.test;

import randoop.ExceptionalExecution;
import randoop.sequence.ExecutableSequence;
import randoop.test.predicate.ExceptionPredicate;

//Dummy failure predicate -- everything fails! --- for tests
public class DummyFailurePredicate implements ExceptionPredicate {

  @Override
  public boolean test(ExceptionalExecution exec, ExecutableSequence s) {
    return false;
  }
}
