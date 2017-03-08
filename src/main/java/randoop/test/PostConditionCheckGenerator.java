package randoop.test;

import java.util.ArrayList;
import randoop.ExecutionOutcome;
import randoop.NormalExecution;
import randoop.NotExecuted;
import randoop.condition.Condition;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Variable;

/** Checks the given post-condition */
public class PostConditionCheckGenerator implements TestCheckGenerator {

  /** the post-condition */
  private final Condition postcondition;

  /**
   * Create a {@link TestCheckGenerator} to test the given post-condition.
   *
   * @param postcondition the post-condition to be tested in generated {@link TestChecks}
   */
  public PostConditionCheckGenerator(Condition postcondition) {
    this.postcondition = postcondition;
  }

  /**
   * Tests the post-condition against the values in the given {@link ExecutableSequence}, and if the
   * condition is not satisfied returns a {@link ErrorRevealingChecks}.
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
    TestChecks checks;
    if (result instanceof NotExecuted) {
      throw new Error("Abnormal execution in sequence: " + s);
    } else if (result instanceof NormalExecution) {
      ArrayList<Variable> inputs = new ArrayList<>(s.sequence.getInputs(finalIndex));
      inputs.add(s.sequence.getVariable(finalIndex));
      Object[] inputValues = s.getRuntimeInputs(inputs);
      if (s.sequence.getStatement(finalIndex).getOperation().isStatic()) {
        inputValues = addNullReceiver(inputValues);
      }
      if (!postcondition.check(inputValues)) {
        checks = new ErrorRevealingChecks();
      } else {
        checks = new RegressionChecks();
      }
      checks.add(new PostConditionCheck(postcondition, inputs));
    } else { // if execution was exceptional, return empty checks
      checks = new ErrorRevealingChecks();
    }
    return checks;
  }

  private Object[] addNullReceiver(Object[] values) {
    Object[] args = new Object[values.length + 1];
    args[0] = null;
    System.arraycopy(values, 0, args, 1, values.length);
    return args;
  }
}
