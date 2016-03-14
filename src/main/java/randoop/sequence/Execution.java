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
  protected final List<ExecutionOutcome> theList;

  // The sequence whose execution results this object stores.
  protected final Sequence owner;

  private Set<Class<?>> coveredClasses;

  /**
   * Create an Execution to store the execution results of the given sequence.
   * The list of outcomes is initialized to NotExecuted for every statement.
   *
   * @param owner  the executed sequence
   */
  public Execution(Sequence owner) {
    this.owner = owner;
    this.theList = new ArrayList<ExecutionOutcome>(owner.size());
    for (int i = 0; i < owner.size(); i++) {
      theList.add(NotExecuted.create());
    }
    this.coveredClasses = new LinkedHashSet<>();
  }

  /**
   * Construct an Execution directly from the given arguments.
   *
   * Do not use this constructor! (Unless you know what you're doing.)
   *
   * @param owner  the executed Sequence
   * @param theList  the list of statement outcomes
   */
  public Execution(Sequence owner, List<ExecutionOutcome> theList) {
    this.owner = owner;
    this.theList = theList;
  }

  /**
   * The size of the list.
   *
   * @return the size of the list
   */
  public int size() {
    return theList.size();
  }

  /** Set the i-th slot to the given outcome. */
  public void set(int i, ExecutionOutcome outcome) {
    if (i < 0 || i >= theList.size()) throw new IllegalArgumentException("wrong index " + i);
    if (outcome == null) throw new IllegalArgumentException("outcome cannot be null.");
    theList.set(i, outcome);
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

  public void addCoveredClass(Class<?> c) {
    coveredClasses.add(c);
  }

  public Set<Class<?>> getCoveredClasses() {
    return coveredClasses;
  }
}
