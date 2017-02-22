package randoop.test;

import java.util.List;

import randoop.ExecutionOutcome;
import randoop.NormalExecution;
import randoop.condition.Condition;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Variable;

/**
 * Checks the given post-condition
 */
public class PostConditionCheckGenerator implements TestCheckGenerator {

  /** the post-condition */
  private final Condition postcondition;

  public PostConditionCheckGenerator(Condition postcondition) {
    this.postcondition = postcondition;
  }

  @Override
  public TestChecks visit(ExecutableSequence s) {
    int lastStatementPos = s.sequence.size() - 1;
    ExecutionOutcome result = s.getResult(lastStatementPos);
    if (result instanceof NormalExecution) {
      List<Variable> inputs = s.sequence.getInputs(lastStatementPos);
      //inputValues = s.getRuntimeValuesForVars()
    }
    return null;
  }
}
