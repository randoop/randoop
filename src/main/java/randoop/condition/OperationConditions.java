package randoop.condition;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * The executable version of an {@link randoop.condition.specification.OperationSpecification}. It
 * is represented as the collection of all {@link BooleanExpression}, {@link GuardPropertyPair}, and
 * {@link GuardThrowsPair} for the {@link randoop.condition.specification.Precondition}, {@link
 * randoop.condition.specification.Postcondition}, and {@link
 * randoop.condition.specification.ThrowsCondition} objects defined on a single operation. Includes
 * specifications inherited from supertypes.
 */
public class OperationConditions {

  /**
   * The {@link BooleanExpression} objects for the {@link
   * randoop.condition.specification.Precondition}s of the operation.
   */
  private final List<BooleanExpression> preExpressions;

  /**
   * The {@link GuardThrowsPair} objects for the {@link
   * randoop.condition.specification.Postcondition}s of the operation.
   */
  private final List<GuardPropertyPair> guardPropertyPairs;

  /**
   * The {@link GuardThrowsPair} objects for the {@link
   * randoop.condition.specification.ThrowsCondition}s of the operation.
   */
  private final List<GuardThrowsPair> guardThrowsPairs;

  /**
   * Mirrors the overrides/implements relation among methods. If this OperationConditions is the
   * local specification for method declaration m, the {@code parentList} contains one element for
   * each method that m overrides or implements (and has specifications).
   *
   * <p>For an operation that is a method, the {@link OperationConditions} form an arbitrary
   * directed acyclic graph consisting of {@link OperationConditions} objects for methods of
   * supertypes, each of which has associated specifications in {@link SpecificationCollection}.
   */
  private List<OperationConditions> parentList = new ArrayList<>();

  /** Creates an empty {@link OperationConditions} object. */
  OperationConditions() {
    this(
        new ArrayList<BooleanExpression>(),
        new ArrayList<GuardPropertyPair>(),
        new ArrayList<GuardThrowsPair>());
  }

  /**
   * Creates an {@link OperationConditions} object for the lists of guard expressions for
   * pre-specifications, {@link GuardPropertyPair} objects for post-specifications, and {@link
   * GuardThrowsPair} objects for throws-specifications.
   *
   * @param preExpressions the guard {@link BooleanExpression} objects for the operation
   *     pre-specifications
   * @param guardPropertyPairs the {@link GuardPropertyPair} objects for the operation
   *     post-specifications
   * @param guardThrowsPairs the {@link GuardThrowsPair} objects for the operation
   *     throws-specifications
   */
  OperationConditions(
      List<BooleanExpression> preExpressions,
      List<GuardPropertyPair> guardPropertyPairs,
      List<GuardThrowsPair> guardThrowsPairs) {
    this.preExpressions = preExpressions;
    this.guardPropertyPairs = guardPropertyPairs;
    this.guardThrowsPairs = guardThrowsPairs;
  }

  /**
   * Check all guard expressions of the method's full specification, which includes this {@link
   * OperationConditions} and those for of any overridden/implemented method.
   *
   * <p>This method makes multiple calls to {@link #checkPrestate(Object[], ExpectedOutcomeTable)},
   * using a new {@link ExpectedOutcomeTable} which is returned.
   *
   * @param args the argument values to test the guard expressions
   * @return the table with entries for this operation
   * @see #checkPrestate(Object[], ExpectedOutcomeTable)
   */
  public ExpectedOutcomeTable checkPrestate(Object[] args) {
    ExpectedOutcomeTable table = new ExpectedOutcomeTable();
    this.checkPrestate(args, table);
    for (OperationConditions conditions : parentList) {
      conditions.checkPrestate(args, table);
    }
    return table;
  }

  /**
   * Modifies the given table, adding an {@link ExpectedOutcomeTable} entry for the guard
   * expressions of this method's local specification recording the following:
   *
   * <ol>
   *   <li>Whether the {@link #preExpressions} fail or are satisfied. See {@link
   *       randoop.condition.OperationConditions#checkPreExpressions(java.lang.Object[])}.
   *   <li>A set of {@link ThrowsClause} objects for expected exceptions. See {@link
   *       randoop.condition.OperationConditions#checkGuardThrowsPairs(java.lang.Object[])}.
   *   <li>The expected {@link BooleanExpression}, if any. See {@link
   *       randoop.condition.OperationConditions#checkGuardPropertyPairs(java.lang.Object[])}.
   * </ol>
   *
   * <p>See the evaluation algorithm in {@link randoop.condition} for more details.
   *
   * @param args the argument values
   * @param table the table to which the created entry is to be added
   */
  private void checkPrestate(Object[] args, ExpectedOutcomeTable table) {
    boolean preconditionCheck = checkPreExpressions(args);
    Set<ThrowsClause> throwsClauses = checkGuardThrowsPairs(args);
    BooleanExpression postCondition = checkGuardPropertyPairs(args);
    table.add(preconditionCheck, postCondition, throwsClauses);
  }

  /**
   * Tests the given argument values against the local preconditions &mdash; that is, the {@link
   * BooleanExpression} objects in {@link #preExpressions} in this {@link OperationConditions}.
   *
   * @param args the argument values
   * @return false if any local precondition fails on the argument values, true if all succeed
   */
  private boolean checkPreExpressions(Object[] args) {
    for (BooleanExpression preCondition : preExpressions) {
      if (!preCondition.check(args)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Evaluate the guard of each local {@link GuardThrowsPair}, and for each one satisfied, add the
   * {@link ThrowsClause} to the set of expected exceptions.
   *
   * @param args the argument values
   * @return the set of exceptions for which the guard expression evaluated to true
   */
  private Set<ThrowsClause> checkGuardThrowsPairs(Object[] args) {
    Set<ThrowsClause> throwsClauses = new LinkedHashSet<>();
    for (GuardThrowsPair pair : guardThrowsPairs) {
      BooleanExpression guardExpression = pair.guardExpression;
      if (guardExpression.check(args)) {
        throwsClauses.add(pair.throwsClause);
      }
    }
    return throwsClauses;
  }

  /**
   * Tests the given argument values against the guards of local postconditions &mdash; that is, the
   * {@link GuardPropertyPair} objects in this {@link OperationConditions}. Returns the {@link
   * BooleanExpression} from the first pair whose guard expression evaluated to true.
   *
   * @param args the argument values
   * @return the {@link BooleanExpression} for the first {@link GuardPropertyPair} for which the
   *     guard expression evaluates to true; null if there is none
   */
  private BooleanExpression checkGuardPropertyPairs(Object[] args) {
    for (GuardPropertyPair pair : guardPropertyPairs) {
      BooleanExpression guardExpression = pair.guardExpression;
      if (guardExpression.check(args)) {
        return pair.booleanExpression.addPrestate(args);
      }
    }
    return null;
  }

  /**
   * Add the parent {@link OperationConditions} for this collection.
   *
   * @param parentConditions the {@link OperationConditions} to which to link
   */
  void addParent(OperationConditions parentConditions) {
    parentList.add(parentConditions);
  }

  /**
   * Indicates whether the full specification is empty: this {@link OperationConditions}, and any
   * member of the parent list, has no guard expresions, no property pairs, and no throws pairs.
   *
   * @return true if there are no guard expressions, or property or throws pairs in this or the
   *     parent list, false otherwise
   */
  public boolean isEmpty() {
    if (!(preExpressions.isEmpty() && guardPropertyPairs.isEmpty() && guardThrowsPairs.isEmpty())) {
      return false;
    }
    for (OperationConditions conditions : parentList) {
      if (!conditions.isEmpty()) {
        return false;
      }
    }
    return true;
  }

  public boolean hasPreconditions() {
    return !preExpressions.isEmpty();
  }
}
