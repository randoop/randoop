package randoop.condition;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import randoop.test.TestCheckGenerator;

/** Records the outcome of checking all of the conditions for a method. */
public class OutcomeTable {
  private boolean isEmpty = true;
  private boolean hasInvalid = false;
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
    } else {
      hasInvalid = true;
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
    return !isEmpty && hasInvalid && exceptionSets.isEmpty();
  }

  public TestCheckGenerator addPostCheckGenerator(TestCheckGenerator gen) {

    //new ExtendGenerator(theNewGenerator, gen);
    return gen;
  }
}
