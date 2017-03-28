package randoop.condition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import plume.Pair;
import randoop.test.ExpectedExceptionGenerator;
import randoop.test.PostConditionCheckGenerator;
import randoop.test.TestCheckGenerator;

/**
 * Represents the conditions on an operation including conditions on parameters, return conditions,
 * and expected exceptions.
 */
public class OperationConditions {

  /** The pre-conditions */
  private final List<Condition> preconditions;

  /** The return-conditions */
  private final List<Pair<Condition, PostCondition>> returnConditions;

  /** The throws-conditions */
  private final Map<Condition, ExpectedExceptionGenerator> throwsConditions;

  /** Conditions of the operation of supertype */
  private OperationConditions supertypeConditions;

  /**
   * Creates an {@link OperationConditions} object for the given pre-conditions, return-conditions,
   * and throws-conditions.
   *
   * @param preconditions the pre-conditions
   * @param returnConditions the return-conditions
   * @param throwsConditions the throws-conditions
   */
  OperationConditions(
      List<Condition> preconditions,
      List<Pair<Condition, PostCondition>> returnConditions,
      Map<Condition, ExpectedExceptionGenerator> throwsConditions) {
    this.preconditions = preconditions;
    this.returnConditions = returnConditions;
    this.throwsConditions = throwsConditions;
  }

  /**
   * Tests the given argument values against the preconditions in this {@link OperationConditions}.
   *
   * @param args the argument values
   * @return false if some precondition fails on the argument values, true otherwise
   */
  public boolean checkPreconditions(Object[] args) {
    for (Condition condition : preconditions) {
      if (!condition.check(args)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Returns the list of {@link PostConditionCheckGenerator} objects that apply for the operation
   * given the argument values.
   *
   * @param args the argument values
   * @return the {@link TestCheckGenerator} to check the property of the return specification for
   *     which the guard is satisfied by the arguments, {@code null} if no guard was satisfied
   */
  public List<TestCheckGenerator> getReturnCheckGenerator(Object[] args) {
    List<TestCheckGenerator> generators = new ArrayList<>();
    if (this.supertypeConditions != null) {
      generators = supertypeConditions.getReturnCheckGenerator(args);
    }
    for (Pair<Condition, PostCondition> entry : returnConditions) {
      Condition precondition = entry.a;
      if (precondition.check(args)) {
        generators.add(new PostConditionCheckGenerator(entry.b.addPrestate(args)));
        break;
      }
    }
    return generators;
  }

  /**
   * Returns the {@link ExpectedExceptionGenerator} for the throws-specification for which the guard
   * is satisfied by the given arguments.
   *
   * @param args the argument values
   * @return the list of {@link ExpectedExceptionGenerator} objects to check for the expected
   *     exceptions from the throws-specifications for which the guard is satisfied.
   */
  public List<TestCheckGenerator> getThrowsCheckGenerator(Object[] args) {
    List<TestCheckGenerator> generators = new ArrayList<>();
    if (this.supertypeConditions != null) {
      generators = supertypeConditions.getThrowsCheckGenerator(args);
    }
    for (Map.Entry<Condition, ExpectedExceptionGenerator> entry : throwsConditions.entrySet()) {
      Condition precondition = entry.getKey();
      if (precondition.check(args)) {
        generators.add(entry.getValue());
      }
    }
    return generators;
  }
}
