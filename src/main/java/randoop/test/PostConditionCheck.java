package randoop.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import plume.UtilMDE;
import randoop.Globals;
import randoop.condition.BooleanExpression;
import randoop.contract.ObjectContractUtils;
import randoop.sequence.Variable;

/** Represents the observed failure of a post-condition. */
public class PostConditionCheck implements Check {

  /** The post-condition */
  private final List<BooleanExpression> postConditions;

  /** The input variables for the condition */
  private final Variable[] inputVariables;

  /**
   * Creates a {@link PostConditionCheck}
   *
   * @param postConditions the post-condition for this check
   * @param inputVariables the input variables for this condition check
   */
  public PostConditionCheck(List<BooleanExpression> postConditions, List<Variable> inputVariables) {
    this.postConditions = postConditions;
    this.inputVariables = inputVariables.toArray(new Variable[0]);
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof PostConditionCheck)) {
      return false;
    }
    PostConditionCheck other = (PostConditionCheck) object;
    if (!this.postConditions.equals(other.postConditions)) {
      return false;
    }
    if (this.inputVariables.length != other.inputVariables.length) {
      return false;
    }
    for (int i = 0; i < this.inputVariables.length; i++) {
      if (!this.inputVariables[i].equals(other.inputVariables[i])) {
        return false;
      }
    }
    return true;
  }

  @Override
  public int hashCode() {
    return Objects.hash(postConditions, Arrays.hashCode(inputVariables));
  }

  @Override
  public String toString() {
    List<String> conditionStrings = new ArrayList<>();
    for (BooleanExpression condition : postConditions) {
      conditionStrings.add(condition.getContractSource());
    }
    return UtilMDE.join(conditionStrings, "&&");
  }

  @Override
  public String toCodeStringPreStatement() {
    return ""; // TODO should be comment with precondition
  }

  @Override
  public String toCodeStringPostStatement() {
    StringBuilder builder = new StringBuilder();
    for (BooleanExpression postCondition : postConditions) {
      String conditionString =
          ObjectContractUtils.localizeContractCode(
              postCondition.getContractSource(), inputVariables);
      builder
          .append("// Checks the post-condition: ")
          .append(postCondition.getComment())
          .append(Globals.lineSep);
      //TODO output whether postcondition failed or succeeded
      builder
          .append("org.junit.Assert.assertTrue( \"Post-condition: ")
          .append(postCondition.getComment())
          .append("\",")
          .append(conditionString)
          .append(");")
          .append(Globals.lineSep);
    }
    return builder.toString();
  }

  /**
   * Get the list of {@link BooleanExpression} objects for this {@link PostConditionCheck}.
   *
   * @return the {@link BooleanExpression} list for this check
   */
  public List<BooleanExpression> getPostConditions() {
    return postConditions;
  }
}
