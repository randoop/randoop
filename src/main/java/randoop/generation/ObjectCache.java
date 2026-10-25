package randoop.generation;

import randoop.NormalExecution;
import randoop.sequence.ExecutableSequence;
import randoop.util.Log;

public class ObjectCache {

  private StateMatcher sm;

  public ObjectCache(StateMatcher sm) {
    this.sm = sm;
  }

  /**
   * Adds the ith value created by the given sequence to this object cache, and clears the active
   * flag of index i in the sequence if the value is not added.
   *
   * @param eseq the sequence that creates values
   * @param i the index of the value to put in this cache
   */
  public void setActiveFlags(ExecutableSequence eseq, int i) {

    assert eseq.getResult(i) instanceof NormalExecution;
    NormalExecution e = (NormalExecution) eseq.getResult(i);

    // If runtime value is not added to the object cache, clear active flag.
    if (!this.sm.add(e.getRuntimeValue())) {
      Log.logPrintf("Making index %d inactive (not added to the object cache)%n", i);
      eseq.sequence.clearActiveFlag(i);
    } else {
      Log.logPrintf("Making index %d active (new value)%n", i);
    }
  }
}
