package randoop.condition;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * The executable version of an {@link randoop.condition.specification.OperationSpecification}. It
 * allows the underlying Boolean expressions to be evaluated.
 *
 * <p>It is represented as three collections: a collection of {@link ExecutableBooleanExpression}
 * for the {@link randoop.condition.specification.Precondition}, a collection of {@link
 * GuardPropertyPair} for the {@link randoop.condition.specification.Postcondition}, and a
 * collection of {@link GuardThrowsPair} for the {@link
 * randoop.condition.specification.ThrowsCondition} in a specification.
 *
 * <p>It includes specifications inherited from supertypes.
 */
public class ExecutableSpecification {

  /**
   * The {@link ExecutableBooleanExpression} objects for the {@link
   * randoop.condition.specification.Precondition}s of the operation.
   */
  private final List<ExecutableBooleanExpression> preExpressions;

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
   * Mirrors the overrides/implements relation among methods. If this ExecutableSpecification is the
   * local specification for method declaration m, the {@code parentList} contains one element for
   * each method that m overrides or implements (and has specifications).
   */
  private List<ExecutableSpecification> parentList = new ArrayList<>();

  /** Creates an empty {@link ExecutableSpecification} object. */
  public ExecutableSpecification() {
    this(new ArrayList<>(0), new ArrayList<>(0), new ArrayList<>(0));
  }

  /**
   * Creates an {@link ExecutableSpecification} object for the lists of guard expressions for
   * pre-specifications, {@link GuardPropertyPair} objects for post-specifications, and {@link
   * GuardThrowsPair} objects for throws-specifications.
   *
   * @param preExpressions the operation pre-specifications
   * @param guardPropertyPairs the operation post-specifications
   * @param guardThrowsPairs the operation throws-specifications
   */
  public ExecutableSpecification(
      List<ExecutableBooleanExpression> preExpressions,
      List<GuardPropertyPair> guardPropertyPairs,
      List<GuardThrowsPair> guardThrowsPairs) {
    this.preExpressions = preExpressions;
    this.guardPropertyPairs = guardPropertyPairs;
    this.guardThrowsPairs = guardThrowsPairs;
  }

  /**
   * Check all guard expressions of the method's full specification, which includes this {@link
   * ExecutableSpecification} and those of any overridden/implemented method.
   *
   * @param args the argument values to test the guard expressions; always includes a receiver (null
   *     for static methods)
   * @return the table with entries for this operation
   * @see #checkPrestate(Object[], ExpectedOutcomeTable)
   */
  public ExpectedOutcomeTable checkPrestate(Object[] args) {
    ExpectedOutcomeTable table = new ExpectedOutcomeTable();
    this.checkPrestate(args, table);
    for (ExecutableSpecification execSpec : parentList) {
      execSpec.checkPrestate(args, table);
    }
    return table;
  }

  /**
   * Modifies the given table, adding an {@link ExpectedOutcomeTable} entry for the guard
   * expressions of this method's local specification recording the following:
   *
   * <ol>
   *   <li>Whether the {@link #preExpressions} fail or are satisfied. See {@link
   *       randoop.condition.ExecutableSpecification#checkPreExpressions(java.lang.Object[])}.
   *   <li>A set of {@link ThrowsClause} objects for expected exceptions. See {@link
   *       randoop.condition.ExecutableSpecification#checkGuardThrowsPairs(java.lang.Object[])}.
   *   <li>The expected {@link ExecutableBooleanExpression}, if any. See {@link
   *       randoop.condition.ExecutableSpecification#checkGuardPropertyPairs(java.lang.Object[])}.
   * </ol>
   *
   * @param args the argument values; always includes a receiver (null for static methods)
   * @param table the table to which the created entry is to be added
   */
  private void checkPrestate(Object[] args, ExpectedOutcomeTable table) {
    boolean preconditionCheck = checkPreExpressions(args);
    List<ThrowsClause> throwsClauses = checkGuardThrowsPairs(args);
    ExecutableBooleanExpression postCondition = checkGuardPropertyPairs(args);
    table.add(preconditionCheck, postCondition, throwsClauses);
  }

  /**
   * Tests the given argument values against the local preconditions, which are the {@link
   * ExecutableBooleanExpression} objects in {@link #preExpressions} in this {@link
   * ExecutableSpecification}.
   *
   * @param args the argument values
   * @return false if any local precondition fails on the argument values, true if all succeed
   */
  private boolean checkPreExpressions(Object[] args) {
    for (ExecutableBooleanExpression preCondition : preExpressions) {
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
   * @return the list of exceptions for which the guard expression evaluated to true
   */
  private List<ThrowsClause> checkGuardThrowsPairs(Object[] args) {
    List<ThrowsClause> throwsClauses = new ArrayList<>();
    for (GuardThrowsPair pair : guardThrowsPairs) {
      ExecutableBooleanExpression guard = pair.guard;
      if (guard.check(args)) {
        throwsClauses.add(pair.throwsClause);
      }
    }
    return throwsClauses;
  }

  /**
   * Tests the given argument values against the guards of local postconditions, which are the
   * {@link GuardPropertyPair} objects in this {@link ExecutableSpecification}. Returns the {@link
   * ExecutableBooleanExpression} from the first pair whose guard expression evaluated to true.
   *
   * @param args the argument values
   * @return the property for the first {@link GuardPropertyPair} for which the guard expression
   *     evaluates to true; null if there is none
   */
  private @Nullable ExecutableBooleanExpression checkGuardPropertyPairs(Object[] args) {
    for (GuardPropertyPair gpPair : guardPropertyPairs) {
      ExecutableBooleanExpression guard = gpPair.guard;
      if (guard.check(args)) {
        return gpPair.property.addPrestate(args);
      }
    }
    return null;
  }

  /**
   * Merges two ExecutableSpecifications into a new one that contains the union of their components.
   *
   * @param spec1 the first ExecutableSpecification to merge
   * @param spec2 the second ExecutableSpecification to merge
   * @return a new ExecutableSpecification that is the result of the merge
   */
  public static ExecutableSpecification merge(
      ExecutableSpecification spec1, ExecutableSpecification spec2) {
    // Merge and remove duplicates for preExpressions
    Set<ExecutableBooleanExpression> mergedPreExpressionsSet =
        new LinkedHashSet<>(spec1.preExpressions);
    mergedPreExpressionsSet.addAll(spec2.preExpressions);
    List<ExecutableBooleanExpression> mergedPreExpressions =
        new ArrayList<>(mergedPreExpressionsSet);

    // Merge and remove duplicates for guardPropertyPairs
    Set<GuardPropertyPair> mergedGuardPropertyPairsSet =
        new LinkedHashSet<>(spec1.guardPropertyPairs);
    mergedGuardPropertyPairsSet.addAll(spec2.guardPropertyPairs);
    List<GuardPropertyPair> mergedGuardPropertyPairs = new ArrayList<>(mergedGuardPropertyPairsSet);

    // Merge and remove duplicates for guardThrowsPairs
    Set<GuardThrowsPair> mergedGuardThrowsPairsSet = new LinkedHashSet<>(spec1.guardThrowsPairs);
    mergedGuardThrowsPairsSet.addAll(spec2.guardThrowsPairs);
    List<GuardThrowsPair> mergedGuardThrowsPairs = new ArrayList<>(mergedGuardThrowsPairsSet);

    return new ExecutableSpecification(
        mergedPreExpressions, mergedGuardPropertyPairs, mergedGuardThrowsPairs);
  }

  /**
   * Add the parent {@link ExecutableSpecification} for this collection.
   *
   * @param parentExecSpec the {@link ExecutableSpecification} to which to link
   */
  void addParent(ExecutableSpecification parentExecSpec) {
    parentList.add(parentExecSpec);
  }

  /**
   * Indicates whether the full specification is empty: this {@link ExecutableSpecification}, and
   * any member of the parent list, has no guard expresions, no property pairs, and no throws pairs.
   *
   * @return true if there are no guard expressions, or property or throws pairs in this or the
   *     parent list, false otherwise
   */
  public boolean isEmpty() {
    if (!(preExpressions.isEmpty() && guardPropertyPairs.isEmpty() && guardThrowsPairs.isEmpty())) {
      return false;
    }
    for (ExecutableSpecification execSpec : parentList) {
      if (!execSpec.isEmpty()) {
        return false;
      }
    }
    return true;
  }

  @Override
  public String toString() {
    return String.format(
        "ExecutableSpecification:  preExpressions=%s  guardPropertyPairs=%s  guardThrowsPairs=%s",
        preExpressions, guardPropertyPairs, guardThrowsPairs);
  }
}
