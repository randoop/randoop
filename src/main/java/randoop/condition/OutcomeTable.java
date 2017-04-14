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
  private final List<Set<ExpectedException>> exceptionSets;
  private final List<PostCondition> postConditions;

  public OutcomeTable() {
    exceptionSets = new ArrayList<>();
    postConditions = new ArrayList<>();
  }

  /**
   * Adds the outcome of checking the conditions of a specification.
   *
   * @param preconditionsSatisfied boolean value indicating whether all preconditions satisfied
   * @param expectedExceptions set of exceptions expected in post-state
   * @param postCondition post-condition that must be true in post-state, null if none
   */
  void add(
      boolean preconditionsSatisfied,
      Set<ExpectedException> expectedExceptions,
      PostCondition postCondition) {
    isEmpty = false;
    if (preconditionsSatisfied) {
      if (postCondition != null) {
        postConditions.add(postCondition);
      }
      hasValid = true;
    }
    if (!expectedExceptions.isEmpty()) {
      exceptionSets.add(expectedExceptions);
    }
  }

  /**
   * Indicate whether this set of results indicates a definitively invalid pre-state. Occurs when
   * all preconditions fail and there are no expected exceptions.
   *
   * @return true if preconditions of all specifications are unsatisfied, and there are no expected
   *     exceptions; false, otherwise
   */
  public boolean isInvalid() {
    return !isEmpty && !hasValid && exceptionSets.isEmpty();
  }

  public TestCheckGenerator addPostCheckGenerator(TestCheckGenerator gen) {
    if (isEmpty) {
      return gen;
    }

    if (!exceptionSets.isEmpty()) {
      if (!hasValid) {
        gen = new InvalidCheckGenerator(); // will be invalid if exception doesn't say otherwise
      }
      return new ExtendGenerator(new ExpectedExceptionGenerator(exceptionSets), gen);
    }

    if (!postConditions.isEmpty()) {
      return new PostConditionCheckGenerator(postConditions);
    }

    return gen;
  }
}
