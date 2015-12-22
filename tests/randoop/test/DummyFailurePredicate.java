package randoop.test;

import randoop.ExceptionalExecution;
import randoop.sequence.ExecutableSequence;
import randoop.test.predicate.DefaultExceptionPredicate;

//Dummy failure predicate -- everything fails! --- for tests
public class DummyFailurePredicate extends DefaultExceptionPredicate {

  @Override
  public boolean test(ExceptionalExecution exec, ExecutableSequence s) {
    return false;
  }

}
