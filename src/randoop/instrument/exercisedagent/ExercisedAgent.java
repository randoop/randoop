package randoop.instrument.exercisedagent;

import java.lang.instrument.Instrumentation;

public class ExercisedAgent {

  public static void premain(String agentArgs, Instrumentation inst) {
    inst.addTransformer(new ExercisedClassTransformer());
  }
}
