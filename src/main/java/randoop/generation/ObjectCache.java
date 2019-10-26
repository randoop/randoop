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
   * Put the ith value created by the given sequence in this object cache.
   *
   * @param eseq the sequence that creates values
   * @param i the index of the value to put in this cache
   */
  public void setActiveFlags(ExecutableSequence eseq, int i) {

    assert eseq.getResult(i) instanceof NormalExecution;
    NormalExecution e = (NormalExecution) eseq.getResult(i);

    // If runtime value is in object cache, clear active flag.
    if (!this.sm.add(e.getRuntimeValue())) {
      Log.logPrintf(
          "Making index %d inactive (already created an object equal to %dth output).%n", i, i);
      eseq.sequence.clearActiveFlag(i);
    } else {
      Log.logPrintf("Making index %d active (new value)%n", i);
    }
  }
}
