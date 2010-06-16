package randoop;

import java.io.Serializable;

import randoop.util.Log;
import randoop.util.PrimitiveTypes;
import randoop.util.Reflection;

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
        if (Log.isLoggingOn())
          Log.logLine("Already created an object equal to " + i + "th output. Making inactive");
        sequence.sequence.clearActiveFlag(i);
      }
  }


}
