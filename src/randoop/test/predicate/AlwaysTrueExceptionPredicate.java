package randoop.test;

import randoop.ExceptionalExecution;
import randoop.sequence.ExecutableSequence;

public class AlwaysTrueExceptionPredicate implements ExceptionPredicate {

  @Override
  public boolean test(ExceptionalExecution exec, ExecutableSequence s) {
    return true;
  }

  @Override
  public ExceptionPredicate or(ExceptionPredicate p) {
    return this;
  }

}
