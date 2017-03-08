package randoop.instrument;

import java.lang.instrument.Instrumentation;

/**
 * Defines the Java agent to instrument classes for exercised-class filtering. Sets up {@code
 * Instrumentation} for JVM so that bytecode first passed through an {@link
 * ExercisedClassTransformer} that performs instrumentation.
 */
public class ExercisedAgent {

  public static void premain(String agentArgs, Instrumentation inst) {
    inst.addTransformer(new ExercisedClassTransformer());
  }
}
