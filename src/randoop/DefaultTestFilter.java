package randoop;

import java.util.LinkedHashSet;
import java.util.Set;

import randoop.main.GenInputsAbstract;

/**
 * <p>Randoop's default test filter determines if a generated test should be output
 * to the user. This determination is made by following these steps in order:</p>
 *
 * <ul>
 * <li> First, if the method sequence did not execute to completion (i.e. an exception occurred in the middle),
 *      do not output, unless the user asked for such tests to be output via --output-nonexec option.
 * <li> Else, if user specified --output-tests=all, output the test.
 * <li> Else, if test revealed no failures, output if user specified --output-tests=pass, otherwise do not output.
 * <li>Else, if test revealed a new failure, output if user specified --output-tests=fail, otherwise do not output.
 *     A test is said to reveal a new failure if its associated FailureSet contains a Failure that is dictinct (via
 *     the equals method) from a failure revealed by any previously-generated tests.
 * </ul>
 *
 * @see randoop.FailureSet
 */
public class DefaultTestFilter implements ITestFilter {

  public Set<FailureSet.Failure> errors = new LinkedHashSet<FailureSet.Failure>();

  @Override
  public boolean outputSequence(ExecutableSequence s, FailureSet f) {

    if (s.hasNonExecutedStatements() && !GenInputsAbstract.output_nonexec) {
      return false;
    }
    
    if (GenInputsAbstract.output_tests.equals(GenInputsAbstract.all)) {
      return true;
    }
    
    if ((GenInputsAbstract.output_tests.equals(GenInputsAbstract.pass))) {
      return f.getFailures().isEmpty();
    }
    
    assert GenInputsAbstract.output_tests.equals(GenInputsAbstract.fail) : GenInputsAbstract.output_tests;

    // FIXME: The filter at this point should return true if this test reveals
    //        a new failure, i.e.:
    //
    //    if (errors.addAll(f.getFailures())) {
    //      return true;
    //    }
    //
    //       The code below behaves slightly differently, returning after only
    //       one successful failure addition to the failure set. This means that
    //       some redundant tests may be generated, since a latter test may reveal
    //       a failure already revealed by the current test.

    for (FailureSet.Failure failure : f.getFailures()) {
      if (errors.add(failure)) {
        if ((GenInputsAbstract.output_tests.equals(GenInputsAbstract.fail))) {
          return true;
        }
      }
    }
    return false;
  }
}
