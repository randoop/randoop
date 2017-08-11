package randoop.condition;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import randoop.test.ExpectedExceptionGenerator;
import randoop.test.ExtendGenerator;
import randoop.test.InvalidCheckGenerator;
import randoop.test.PostConditionCheckGenerator;
import randoop.test.TestCheckGenerator;

/**
 * Records the outcome of checking all of the preconditions for a method. Each table entry records:
 *
 * <p>TODO: clarify the following, or merge it into other documentation.
 *
 * <ol>
 *   <li>Whether the preconditions of the specification fail or are satisfied. The preconditions
 *       fail if the Boolean expression of any precondition in the specification is false.
 *       Otherwise, the preconditions are satisfied. See {@link
 *       OperationConditions#checkPreconditions(java.lang.Object[])}.
 *   <li>A set of expected exceptions. The exception is expected because its guard was satisfied.
 *       Evaluate the guard of each throws-condition, and for each one satisfied, add the exception
 *       to the set of expected exceptions. (There will be one set per specification.) See {@link
 *       OperationConditions#checkThrowsPreconditions(java.lang.Object[])}.
 *   <li>The expected postcondition, if any. If the preconditions are satisfied, test the guards of
 *       the normal postconditions of the specification in order, and save the property for the
 *       first guard satisfied, if there is one. See {@link
 *       OperationConditions#checkPostconditionGuards(java.lang.Object[])}.
 * </ol>
 *
 * <p>TODO: Merge the following in as well
 *
 * <ul>
 *   <li>Whether the preconditions fail or are satisfied, given the observed values
 *   <li>The set of expected exceptions, given the observed values (that is, which
 *       throws-conditions' guards are satisfied)
 *   <li>The expected postcondition, given the observed values (that is, which return-clauses'
 *       guards are satisfied)
 * </ul>
 */
public class ExpectedOutcomeTable {

  /** Indicates whether this table is empty. */
  private boolean isEmpty = true;

  /** Indicates whether a precondition was satisfied. */
  private boolean hasSatisfiedPrecondition = false;

  /** The list of sets of throws clauses for which the pre-condition was satisfied. */
  private final List<Set<ThrowsClause>> exceptionSets;

  /** The list of post-conditions for which the pre-condition was satisfied. */
  private final List<PostCondition> postConditions;

  /** Creates an empty {@link ExpectedOutcomeTable}. */
  public ExpectedOutcomeTable() {
    exceptionSets = new ArrayList<>();
    postConditions = new ArrayList<>();
  }

  /**
   * Adds the outcome of checking the conditions of a specification.
   *
   * @param preconditionsSatisfied boolean value indicating whether all preconditions are satisfied
   * @param throwsClauses set of exception type-comment pairs for exceptions expected in post-state
   * @param postCondition post-condition that must be true in post-state if no exception is thrown,
   *     null if none
   */
  void add(
      boolean preconditionsSatisfied,
      Set<ThrowsClause> throwsClauses,
      PostCondition postCondition) {
    isEmpty = false;
    if (preconditionsSatisfied) {
      if (postCondition != null) {
        postConditions.add(postCondition);
      }
      hasSatisfiedPrecondition = true;
    }
    if (!throwsClauses.isEmpty()) {
      exceptionSets.add(throwsClauses);
    }
  }

  /**
   * Indicate whether this set of results indicates a definitively invalid pre-state. Occurs when
   * all preconditions fail and there are no expected exceptions.
   *
   * <p>This method should be called after all entries are added; that is, no more entries should be
   * added after it is called.
   *
   * @return true if preconditions of all specifications are unsatisfied, and there are no expected
   *     exceptions; false, otherwise
   */
  public boolean isInvalidPrestate() {
    return !isEmpty && !hasSatisfiedPrecondition && exceptionSets.isEmpty();
  }

  /**
   * Constructs the {@link TestCheckGenerator} that will test for expected conditions as follows:
   *
   * <ul>
   *   <li>if this table is empty, returns the given generator.
   *   <li>if this table has expected exceptions, then returns a generator that checks for those
   *       exceptions.
   *   <li>if all preconditions fail, then return an {@link InvalidCheckGenerator}.
   *   <li>if there are post-conditions, then extend the given generator with a {@link
   *       PostConditionCheckGenerator}.
   * </ul>
   *
   * @param gen the generator to extend
   * @return the {@link TestCheckGenerator} to check for expected outcomes in this table
   */
  public TestCheckGenerator addPostCheckGenerator(TestCheckGenerator gen) {
    if (isEmpty) {
      return gen;
    }

    // if there are expected exceptions, then override pre-conditions
    if (!exceptionSets.isEmpty()) {
      return new ExpectedExceptionGenerator(exceptionSets);
    }

    // had conflict with pre-conditions
    if (!hasSatisfiedPrecondition) {
      gen = new InvalidCheckGenerator();
    }

    if (!postConditions.isEmpty()) {
      return new ExtendGenerator(new PostConditionCheckGenerator(postConditions), gen);
    }

    return gen;
  }
}
