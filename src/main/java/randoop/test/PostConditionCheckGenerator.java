package randoop.test;

import java.util.ArrayList;
import java.util.List;
import randoop.ExecutionOutcome;
import randoop.NormalExecution;
import randoop.NotExecuted;
import randoop.condition.PostCondition;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Variable;

/** Checks the given post-conditions after the last statement of a sequence. */
public class PostConditionCheckGenerator implements TestCheckGenerator {

  /** the post-conditions */
  private final List<PostCondition> postConditions;

  /**
   * Create a {@link TestCheckGenerator} to test the given post-condition.
   *
   * @param postConditions the post-condition to be tested in generated {@link TestChecks}
   */
  public PostConditionCheckGenerator(List<PostCondition> postConditions) {
    this.postConditions = postConditions;
  }

  /**
   * Tests all of the the post-conditions against the values in the given {@link
   * ExecutableSequence}, and if the condition is not satisfied returns a {@link
   * ErrorRevealingChecks}.
   *
   * <p>Note that the operation input values passed to the post-condition are the values
   * post-execution.
   *
   * @param s the sequence for which checks are generated
   * @return the {@link ErrorRevealingChecks} with a {@link PostConditionCheck} if the
   *     post-condition fails on the sequence, an {@code null} otherwise
   */
  @Override
  public TestChecks visit(ExecutableSequence s) {
    int finalIndex = s.sequence.size() - 1;
    ExecutionOutcome result = s.getResult(finalIndex);

    s.conditionType = ExecutableSequence.ConditionType.PARAM;

    TestChecks checks;
    if (result instanceof NotExecuted) {
      throw new Error("Abnormal execution in sequence: " + s);
    } else if (result instanceof NormalExecution) {
      List<PostCondition> failed = new ArrayList<>();
      ArrayList<Variable> inputs = new ArrayList<>(s.sequence.getInputs(finalIndex));
      inputs.add(s.sequence.getVariable(finalIndex));
      Object[] inputValues = s.getRuntimeInputs(inputs);
      if (s.sequence.getStatement(finalIndex).getOperation().isStatic()) {
        inputValues = addNullReceiver(inputValues);
      }
      for (PostCondition postCondition : postConditions) {
        if (!postCondition.check(inputValues)) {
          failed.add(postCondition);
        }
      }
      if (failed.isEmpty()) {
        checks = new RegressionChecks();
        checks.add(new PostConditionCheck(postConditions, inputs));
      } else {
        checks = new ErrorRevealingChecks();
        checks.add(new PostConditionCheck(failed, inputs));
      }

      s.sequence.disableShortForm();
    } else { // if execution was exceptional, return empty checks
      checks = new ErrorRevealingChecks();
    }
    return checks;
  }

  @Override
  public TestCheckGenerator getGenerator() {
    return this;
  }

  private Object[] addNullReceiver(Object[] values) {
    Object[] args = new Object[values.length + 1];
    args[0] = null;
    System.arraycopy(values, 0, args, 1, values.length);
    return args;
  }
}
