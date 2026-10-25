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

  /** The execution outcome of each statement. */
  /*package*/ final List<ExecutionOutcome> outcomes;

  /** The instrumented classes that were covered by the execution. */
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
   * Returns the outcome in the i-th slot.
   *
   * @param i the statement position
   * @return the outcome of the ith statement
   */
  public ExecutionOutcome get(int i) {
    return outcomes.get(i);
  }

  /**
   * Records that the execution covered the given class.
   *
   * @param c a class that the execution covered
   */
  /*package*/ void addCoveredClass(Class<?> c) {
    coveredClasses.add(c);
  }

  /**
   * Returns the classes that the execution covered.
   *
   * @return the classes that the execution covered
   */
  /*package*/ Set<Class<?>> getCoveredClasses() {
    return coveredClasses;
  }
}
