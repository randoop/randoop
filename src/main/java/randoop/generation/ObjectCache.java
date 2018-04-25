package randoop.generation;

import randoop.NormalExecution;
import randoop.sequence.ExecutableSequence;
import randoop.util.Log;

public class ObjectCache {

  private StateMatcher sm;

  public ObjectCache(StateMatcher sm) {
    this.sm = sm;
  }

  public void setActiveFlags(ExecutableSequence sequence, int i) {

    assert sequence.getResult(i) instanceof NormalExecution;
    NormalExecution e = (NormalExecution) sequence.getResult(i);

    // If runtime value is in object cache, clear active flag.
    if (!this.sm.add(e.getRuntimeValue())) {
      Log.logPrintf(
          "Making index %d inactive (already created an object equal to %dth output).%n", i, i);
      sequence.sequence.clearActiveFlag(i);
    } else {
      Log.logPrintf("Making index %d active (new value)%n", i);
    }
  }
}
