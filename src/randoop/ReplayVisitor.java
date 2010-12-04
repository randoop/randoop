package randoop;

import java.util.ArrayList;

/**
 * An execution visitor that evaluates the checks in the
 * given executable sequence, and returns <code>false</code>
 * if any of them fail.
 */
public class ReplayVisitor implements ExecutionVisitor {

  @Override
  public void initialize(ExecutableSequence s) {
    s.checksResults.clear();
    for (int i = 0 ; i < s.sequence.size() ; i++) {
      s.checksResults.add(new ArrayList<Boolean>(1));
    }
  }
  
  @Override
  public void visitAfter(ExecutableSequence es, int i) {
    if (!es.hasChecks(i)) {
      return; // No checks here; nothing to do.
    }
    for (Check c : es.getChecks(i)) {
      boolean checkResult = c.evaluate(es.executionResults);
      es.checksResults.get(i).add(checkResult);
    }
  }

  @Override
  public void visitBefore(ExecutableSequence sequence, int i) {
    // Empty body: nothing to before the statement is executed.
  }


}
