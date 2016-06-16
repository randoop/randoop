package randoop.sequence;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import randoop.ExecutionOutcome;
import randoop.NotExecuted;

/**
 * Represents the unfolding execution of a sequence.
 *
 * This is essentially a wrapper around List&lt;ExecutionOutcome&gt;. Stores
 * information in a list of ExecutionOutcome objects, one for each statement in
 * the sequence.
 */
public final class Execution {

  // The execution outcome of each statement.
  final List<ExecutionOutcome> theList;

  private Set<Class<?>> coveredClasses;

  /**
   * Create an Execution to store the execution results of the given sequence.
   * The list of outcomes is initialized to NotExecuted for every statement.
   *
   * @param owner  the executed sequence
   */
  public Execution(Sequence owner) {
    this.theList = new ArrayList<>(owner.size());
    for (int i = 0; i < owner.size(); i++) {
      theList.add(NotExecuted.create());
    }
    this.coveredClasses = new LinkedHashSet<>();
  }

  /**
   * The size of the list.
   *
   * @return the size of the list
   */
  public int size() {
    return theList.size();
  }

  /**
   * Get the outcome in the i-th slot.
   *
   * @param i  the statement position
   * @return the outcome of the ith statement
   */
  public ExecutionOutcome get(int i) {
    if (i < 0 || i >= theList.size()) throw new IllegalArgumentException("wrong index.");
    return theList.get(i);
  }

  void addCoveredClass(Class<?> c) {
    coveredClasses.add(c);
  }

  Set<Class<?>> getCoveredClasses() {
    return coveredClasses;
  }
}
