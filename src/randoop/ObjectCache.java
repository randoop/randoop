package randoop;

import java.io.Serializable;

import randoop.util.Log;

public class ObjectCache implements Serializable {

  private static final long serialVersionUID = -8051750221965948545L;
  private StateMatcher sm;

  public ObjectCache(StateMatcher sm) {
    this.sm = sm;
  }

  public void setActiveFlags(ExecutableSequence sequence, int i) {

      assert sequence.getResult(i) instanceof NormalExecution;
      NormalExecution e = (NormalExecution)sequence.getResult(i);
    
      // If runtime value is in object cache, clear active flag.
      if (!this.sm.add(e.getRuntimeValue())) {
        if (Log.isLoggingOn()) {
          Log.logLine("Making index " + i + " inactive (already created an object equal to " + i + "th output).");
        }
        sequence.sequence.clearActiveFlag(i);
      } else {
        if (Log.isLoggingOn()) {
          Log.logLine("Making index " + i + " active (new value)");
        }        
      }
  }


}
