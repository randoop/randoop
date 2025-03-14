package randoop.sequence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import randoop.ExecutionOutcome;
import randoop.NotExecuted;

/**
 * Represents the unfolding execution of a sequence.
 *
 * <p>This is essentially a wrapper around {@code List<ExecutionOutcome>}. Stores information in a
 * list of ExecutionOutcome objects, one for each statement in the sequence.
 */
public final class Execution {

  // The execution outcome of each statement.
  final List<ExecutionOutcome> outcomes;

  private Set<Class<?>> coveredClasses;

  /**
   * Create an Execution to store the execution results of the given sequence. The list of outcomes
   * is initialized to NotExecuted for every statement.
   *
   * @param owner the executed sequence
   */
  public Execution(Sequence owner) {
    // The `outcomes` list will be modified later.  (Collections.nCopies is immutable.)
    this.outcomes = new ArrayList<>(Collections.nCopies(owner.size(), NotExecuted.create()));
    this.coveredClasses = new LinkedHashSet<>();
  }

  /**
   * The size of the list.
   *
   * @return the size of the list
   */
  public int size() {
    return outcomes.size();
  }

  /**
   * Get the outcome in the i-th slot.
   *
   * @param i the statement position
   * @return the outcome of the ith statement
   */
  public ExecutionOutcome get(int i) {
    return outcomes.get(i);
  }

  void addCoveredClass(Class<?> c) {
    coveredClasses.add(c);
  }

  Set<Class<?>> getCoveredClasses() {
    return coveredClasses;
  }
}
