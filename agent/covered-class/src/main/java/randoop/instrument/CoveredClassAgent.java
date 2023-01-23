package randoop.instrument;

import java.lang.instrument.Instrumentation;

/**
 * Defines the Java agent to instrument classes for covered-class filtering. Sets up {@code
 * Instrumentation} for JVM so that bytecode first passed through an {@link CoveredClassTransformer}
 * that performs instrumentation.
 */
public class CoveredClassAgent {

  /**
   * The premain method that instruments classes.
   *
   * @param agentArgs agent options
   * @param inst the instrumentation
   */
  public static void premain(String agentArgs, Instrumentation inst) {
    inst.addTransformer(new CoveredClassTransformer());
  }
}
