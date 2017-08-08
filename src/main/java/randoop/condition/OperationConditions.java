package randoop.condition;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents all conditions on an operation including pre-, post- and throws-conditions defined on
 * this operation and inherited from supertypes.
 *
 * <p>For an operation that is a method, the {@link OperationConditions} form an arbitrary directed
 * acyclic graph consisting of conditions for methods of supertypes, each of which has attached
 * specifications.
 */
public class OperationConditions {

  /** The pre-conditions for the operation */
  private final List<Condition> preconditions;

  /** The return-conditions. */
  private final List<ConditionPair<PostCondition>> returnConditions;

  /** The throws-conditions. */
  private final List<ConditionPair<ThrowsClause>> throwsConditions;

  /** The parent conditions for this object */
  private List<OperationConditions> parentList;

  /** Creates an empty {@link OperationConditions} object. */
  OperationConditions() {
    this(
        new ArrayList<Condition>(),
        new ArrayList<ConditionPair<PostCondition>>(),
        new ArrayList<ConditionPair<ThrowsClause>>());
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
      List<ConditionPair<PostCondition>> returnConditions,
      List<ConditionPair<ThrowsClause>> throwsConditions) {
    this.preconditions = preconditions;
    this.returnConditions = returnConditions;
    this.throwsConditions = throwsConditions;
    this.parentList = new ArrayList<>();
  }

  /**
   * Check the conditions for this operation against the arguments. Constructs an {@link
   * OutcomeTable} with an entry for the conditions for this operation, and conditions for this
   * operation in all supertypes.
   *
   * @param args the argument values to test the conditions
   * @return the table with entries for this operation
   * @see #check(Object[], OutcomeTable)
   */
  public OutcomeTable check(Object[] args) {
    OutcomeTable table = new OutcomeTable();
    this.check(args, table);
    for (OperationConditions conditions : parentList) {
      conditions.check(args, table);
    }
    return table;
  }

  /**
   * Creates an {@link OutcomeTable} entry for the conditions of this method, recording:
   *
   * <ul>
   *   <li>Whether the preconditions fail or are satisfied
   *   <li>The set of expected exceptions
   *   <li>The expected postcondition
   * </ul>
   *
   * (See the evaluation algorithm in {@link randoop.condition}.)
   *
   * @param args the argument values
   * @param table the table to which the created entry is to be added
   */
  private void check(Object[] args, OutcomeTable table) {
    boolean preconditionCheck = checkPreconditions(args);
    Set<ThrowsClause> throwsClauses = checkThrowsConditions(args);
    PostCondition postCondition = checkPostconditions(args);
    table.add(preconditionCheck, throwsClauses, postCondition);
  }

  /**
   * Tests the given argument values against the preconditions in this {@link OperationConditions}.
   * The preconditions fail if any precondition evaluates to value on the arguments.
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

  /**
   * Tests the given argument values against the guards of the throws-conditions in this {@link
   * OperationConditions} and returns the set of exceptions whose precondition evaluated to true.
   *
   * @param args the argument values
   * @return the set of exceptions for which the precondition evaluated to true
   */
  private Set<ThrowsClause> checkThrowsConditions(Object[] args) {
    Set<ThrowsClause> throwsClauses = new LinkedHashSet<>();
    for (ConditionPair<ThrowsClause> pair : throwsConditions) {
      Condition precondition = pair.preCondition;
      if (precondition.check(args)) {
        throwsClauses.add(pair.postClause);
      }
    }
    return throwsClauses;
  }

  /**
   * Tests the given argument values against the guards of the post-conditions in this {@link
   * OperationConditions} and returns the first post-condition whose precondition evaluated to true.
   *
   * @param args the argument values
   * @return the first post-condition for which the precodition evaluates to true; null if there is
   *     none
   */
  private PostCondition checkPostconditions(Object[] args) {
    for (ConditionPair<PostCondition> pair : returnConditions) {
      Condition precondition = pair.preCondition;
      if (precondition.check(args)) {
        return pair.postClause.addPrestate(args);
      }
    }
    return null;
  }

  /**
   * Add the parent {@link OperationConditions} for this collection of conditions.
   *
   * @param parentConditions the {@link OperationConditions} to which to link
   */
  void addParent(OperationConditions parentConditions) {
    parentList.add(parentConditions);
  }

  /**
   * Indicates whether this {@link OperationConditions}, and the parent, has no conditions.
   *
   * @return true if there are no pre-, post- or throws-conditions in this or the parent, false
   *     otherwise
   */
  public boolean isEmpty() {
    if (preconditions.isEmpty() && returnConditions.isEmpty() && throwsConditions.isEmpty()) {
      for (OperationConditions conditions : parentList) {
        if (!conditions.isEmpty()) {
          return false;
        }
      }
      return true;
    }
    return false;
  }
}
