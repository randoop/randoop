package randoop.test;

import java.util.ArrayList;
import java.util.List;
import randoop.ExecutionOutcome;
import randoop.NormalExecution;
import randoop.NotExecuted;
import randoop.condition.ExecutableBooleanExpression;
import randoop.sequence.DummyVariable;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Variable;

/** Checks the given post-conditions after the last statement of a sequence. */
public class PostConditionCheckGenerator extends TestCheckGenerator {

  /** the post-conditions */
  private final List<ExecutableBooleanExpression> postConditions;

  /**
   * Create a {@link TestCheckGenerator} to test the given post-condition.
   *
   * @param postConditions the post-condition to be tested in generated {@link TestChecks}
   */
  public PostConditionCheckGenerator(List<ExecutableBooleanExpression> postConditions) {
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
   * @param eseq the sequence for which checks are generated
   * @return the {@link ErrorRevealingChecks} with a {@link PostConditionCheck} if the
   *     post-condition fails on the sequence, an {@code null} otherwise
   */
  @Override
  public TestChecks<?> generateTestChecks(ExecutableSequence eseq) {
    int finalIndex = eseq.sequence.size() - 1;
    ExecutionOutcome result = eseq.getResult(finalIndex);
    if (result instanceof NotExecuted) {
      throw new Error("Abnormal execution in sequence: " + eseq);
    } else if (result instanceof NormalExecution) {
      ArrayList<Variable> inputs = new ArrayList<>(eseq.sequence.getInputs(finalIndex));
      inputs.add(eseq.sequence.getVariable(finalIndex));
      Object[] inputValues = eseq.getRuntimeInputs(inputs);
      if (eseq.sequence.getStatement(finalIndex).getOperation().isStatic()) {
        inputValues = addNullReceiver(inputValues);
        inputs.add(0, DummyVariable.DUMMY);
      }

      List<ExecutableBooleanExpression> failed = new ArrayList<>();
      for (ExecutableBooleanExpression postCondition : postConditions) {
        if (!postCondition.check(inputValues)) {
          failed.add(postCondition);
        }
      }
      eseq.sequence.doNotInlineLiterals();
      if (failed.isEmpty()) {
        return new RegressionChecks(new PostConditionCheck(postConditions, inputs));
      } else {
        return new ErrorRevealingChecks(new PostConditionCheck(failed, inputs));
      }
    } else { // if execution was exceptional, return empty checks
      return ErrorRevealingChecks.EMPTY;
    }
  }

  private Object[] addNullReceiver(Object[] values) {
    Object[] args = new Object[values.length + 1];
    args[0] = null;
    System.arraycopy(values, 0, args, 1, values.length);
    return args;
  }
}
