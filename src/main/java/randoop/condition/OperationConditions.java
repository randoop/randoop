package randoop.condition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import plume.Pair;

/**
 * Represents the conditions on an operation including conditions on parameters, return conditions,
 * and expected exceptions.
 *
 * <p>The {@link OperationConditions} for a method form an arbitrary tree consisting of conditions
 * for methods of supertypes, each of which has attached specifications.
 */
public class OperationConditions {

  /** The pre-conditions */
  private final List<Condition> preconditions;

  /** The return-conditions */
  private final List<Pair<Condition, PostCondition>> returnConditions;

  /** The throws-conditions */
  private final Map<Condition, ExpectedException> throwsConditions;

  /** The parent conditions for this object */
  private List<OperationConditions> parentList;

  /** Creates an empty {@link OperationConditions} object. */
  OperationConditions() {
    this(
        new ArrayList<Condition>(),
        new ArrayList<Pair<Condition, PostCondition>>(),
        new HashMap<Condition, ExpectedException>());
  }

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
      Map<Condition, ExpectedException> throwsConditions) {
    this.preconditions = preconditions;
    this.returnConditions = returnConditions;
    this.throwsConditions = throwsConditions;
    this.parentList = new ArrayList<>();
  }

  public OutcomeTable check(Object[] args) {
    OutcomeTable table = new OutcomeTable();
    this.check(args, table);
    for (OperationConditions conditions : parentList) {
      conditions.check(args, table);
    }
    return table;
  }

  private void check(Object[] args, OutcomeTable table) {
    boolean preconditionCheck = checkPreconditions(args);
    Set<ExpectedException> expectedExceptions = checkThrowsConditions(args);
    PostCondition postCondition = checkPostconditions(args);
    table.add(preconditionCheck, expectedExceptions, postCondition);
  }

  /**
   * Tests the given argument values against the preconditions in this {@link OperationConditions}.
   *
   * @param args the argument values
   * @return false if some precondition fails on the argument values, true otherwise
   */
  private boolean checkPreconditions(Object[] args) {
    for (Condition condition : preconditions) {
      if (!condition.check(args)) {
        return false;
      }
    }
    return true;
  }

  private Set<ExpectedException> checkThrowsConditions(Object[] args) {
    Set<ExpectedException> expectedExceptions = new LinkedHashSet<>();
    for (Map.Entry<Condition, ExpectedException> entry : throwsConditions.entrySet()) {
      Condition precondition = entry.getKey();
      if (precondition.check(args)) {
        expectedExceptions.add(entry.getValue());
      }
    }
    return expectedExceptions;
  }

  private PostCondition checkPostconditions(Object[] args) {
    for (Pair<Condition, PostCondition> entry : returnConditions) {
      Condition precondition = entry.a;
      if (precondition.check(args)) {
        return entry.b.addPrestate(args);
      }
    }
    return null;
  }

  void addParent(OperationConditions parentConditions) {
    parentList.add(parentConditions);
  }

  public boolean isEmpty() {
    return preconditions.isEmpty()
        && returnConditions.isEmpty()
        && throwsConditions.isEmpty()
        && parentList.isEmpty();
  }
}
