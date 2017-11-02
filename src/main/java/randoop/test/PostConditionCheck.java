package randoop.test;

import java.util.ArrayList;
import randoop.Globals;
import randoop.condition.Condition;
import randoop.contract.ObjectContractUtils;
import randoop.sequence.Variable;

/** Represents the observed failure of a post-condition. */
public class PostConditionCheck implements Check {

  /** The post-condition */
  private final Condition postcondition;

  /** The input variables for the condition */
  private final Variable[] inputVariables;

  /**
   * Creates a {@link PostConditionCheck}
   *
   * @param postcondition the post-condition for this check
   * @param inputVariables the input variables for this condition check
   */
  public PostConditionCheck(Condition postcondition, ArrayList<Variable> inputVariables) {
    this.postcondition = postcondition;
    this.inputVariables = inputVariables.toArray(new Variable[0]);
  }

  @Override
  public String toCodeStringPreStatement() {
    return ""; // TODO should be comment with precondition
  }

  @Override
  public String toCodeStringPostStatement() {
    String conditionString =
        ObjectContractUtils.localizeContractCode(
            postcondition.getConditionString(), inputVariables);
    return Globals.lineSep
        + "// Checks the post-condition: "
        + postcondition.getComment()
        + Globals.lineSep
        + "org.junit.Assert.assertTrue("
        + "\"Contract failed: "
        + postcondition.getComment()
        + "\", "
        + conditionString
        + ");";
  }
}
