package randoop.test;

import randoop.ExceptionCheck;
import randoop.sequence.ExecutableSequence;
import randoop.util.TimeoutExceededException;
import randoop.util.predicate.DefaultPredicate;

public class RegressionTestPredicate extends DefaultPredicate<ExecutableSequence> {

  @Override
  public boolean test(ExecutableSequence s) {
    //don't want error revealing test
    if (s.hasFailure()) {
      return false;
    }

    TestChecks testChecks = s.getChecks();
    
    //don't want regression test with no assertions
    if (!testChecks.hasChecks()) {
      return false;
    }
    
    //if have exception
    ExceptionCheck ec = testChecks.getExceptionCheck();
    if (ec != null) {
      // Remove any sequences that throw randoop.util.TimeoutExceededException.
      // It would be nicer for Randoop to output a test suite that detects
      // long-running tests and generates a TimeoutExceededException, as
      // documented in Issue 11:
      // https://github.com/randoop/randoop/issues/11 .
      if (ec.getException() instanceof TimeoutExceededException) {
        return false;
      }
    }
    
    return true;
  }

}
