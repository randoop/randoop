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

  public boolean setActiveFlags(ExecutableSequence sequence) {

    for (int i = 0 ; i < sequence.sequence.size() ; i++) {

      // If statement was not executed, clear active flag.
      if (sequence.getResult(i) instanceof NotExecuted) {
        if (Log.isLoggingOn())
          Log.logLine("Statement " + i + " was not executed (due to failures earlier in the sequence). Making inactive.");
        sequence.sequence.clearActiveFlag(i);
        continue;
      }

      // If exception thrown at index i, clear active flag.
      if (sequence.getResult(i) instanceof ExceptionalExecution) {
        if (Log.isLoggingOn())
          Log.logLine("Statement " + i + " threw exception. Making inactive.");
        sequence.sequence.clearActiveFlag(i);
        continue;
      }

      assert sequence.getResult(i) instanceof NormalExecution;
      NormalExecution e = (NormalExecution)sequence.getResult(i);

      // If runtime value is null, clear active flag.
      if (e.getRuntimeValue() == null) {
        if (Log.isLoggingOn())
          Log.logLine("Object " + i + " is null. Making inactive.");
        sequence.sequence.clearActiveFlag(i);
        continue;
      }

      // Sanity check: object is of the correct type.
      Class<?> objectClass = e.getRuntimeValue().getClass();
      Class<?> constraintType = sequence.sequence.getStatementKind(i).getOutputType();
      if (!Reflection.canBeUsedAs(objectClass, constraintType))
        throw new BugInRandoopException("objectClass=" + objectClass.getName() + ", constraingType=" + constraintType.getName());

      // If runtime value is a primitive value, clear active flag.
      if (PrimitiveTypes.isBoxedOrPrimitiveOrStringType(objectClass)) {
        if (Log.isLoggingOn())
          Log.logLine("Object " + i + " is a primitive. Making inactive.");
        sequence.sequence.clearActiveFlag(i);
        continue;
      }

      // If runtime value is in object cache, clear active flag.
      if (!this.sm.add(e.getRuntimeValue())) {
        if (Log.isLoggingOn())
          Log.logLine("Already created an object equal to " + i + "th output. Making inactive");
        sequence.sequence.clearActiveFlag(i);
        continue;
      }

      if (Log.isLoggingOn())
        Log.logLine("Object " + i + " NOT set to inactive.");

    }
    return sequence.sequence.hasActiveFlags();
  }


}
