package randoop.condition;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import randoop.test.ExpectedExceptionGenerator;
import randoop.test.ExtendGenerator;
import randoop.test.InvalidCheckGenerator;
import randoop.test.PostConditionCheckGenerator;
import randoop.test.TestCheckGenerator;

/** Records the outcome of checking all of the conditions for a method. */
public class OutcomeTable {
  private boolean isEmpty = true;
  private boolean hasValid = false;
  private final List<Set<ThrowsClause>> exceptionSets;
  private final List<PostCondition> postConditions;

  public OutcomeTable() {
    exceptionSets = new ArrayList<>();
    postConditions = new ArrayList<>();
  }

  /**
   * Adds the outcome of checking the conditions of a specification.
   *
   * @param preconditionsSatisfied boolean value indicating whether all preconditions satisfied
   * @param throwsClauses set of exceptions expected in post-state
   * @param postCondition post-condition that must be true in post-state, null if none
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
      hasValid = true;
    }
    if (!throwsClauses.isEmpty()) {
      exceptionSets.add(throwsClauses);
    }
  }

  /**
   * Indicate whether this set of results indicates a definitively invalid pre-state. Occurs when
   * all preconditions fail and there are no expected exceptions.
   *
   * <p>This method will evaluate the current state of the table, but should be called after all
   * entries are added
   *
   * @return true if preconditions of all specifications are unsatisfied, and there are no expected
   *     exceptions; false, otherwise
   */
  public boolean isInvalid() {
    return !isEmpty && !hasValid && exceptionSets.isEmpty();
  }

  /**
   * Constructs the {@link TestCheckGenerator} to apply post call by extending the given generator.
   *
   * <ul>
   *   <li>if this table is empty, returns the given generator
   *   <li>if this table has expected exceptions, then returns a generator that checks for those
   *       exceptions.
   *   <li>if all preconditions fail, then return an {@link InvalidCheckGenerator}.
   *   <li>if there are post-conditions, then extend the given generator with a {@link
   *       PostConditionCheckGenerator}.
   * </ul>
   *
   * (Pre-conditions are checked here to allow for conflicts with throws-conditions.)
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
    if (!hasValid) {
      gen = new InvalidCheckGenerator();
    }

    if (!postConditions.isEmpty()) {
      return new ExtendGenerator(new PostConditionCheckGenerator(postConditions), gen);
    }

    return gen;
  }
}
