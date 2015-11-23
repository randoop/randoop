package randoop;

import randoop.sequence.ExecutableSequence;

import randoop.sequence.ExecutableSequence;

/**
 * An execution visitor that evaluates the checks in the
 * given executable sequence, and returns <code>false</code>
 * if any of them fail.
 */
public class ReplayVisitor implements ExecutionVisitor {

  @Override
  public void initialize(ExecutableSequence s) {
    s.initializeResults();
  }
  
  @Override
  public void visitAfter(ExecutableSequence es, int i) {
    if (es.hasChecks(i)) {
      es.performChecks(i);  
    }
  }

  @Override
  public void visitBefore(ExecutableSequence sequence, int i) {
    // Empty body: nothing to before the statement is executed.
  }


}
