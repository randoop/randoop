package randoop.contract;

import randoop.ExceptionalExecution;
import randoop.ExecutionOutcome;
import randoop.NormalExecution;
import randoop.sequence.Variable;
import randoop.util.ObjectContractReflectionCode;
import randoop.util.ReflectionCode;
import randoop.util.ReflectionExecutor;
import randoop.util.Timer;

/** Utility methods for safely executing and printing {@link ObjectContract} code. */
public class ObjectContractUtils {

  private ObjectContractUtils() {
    throw new Error("Do not instantiate");
  }

  /**
   * Executes the given contract via reflection.
   *
   * @param c the contract to execute
   * @param objs the list of values to substitute for variables
   * @return the outcome from the execution
   */
  public static ExecutionOutcome execute(final ObjectContract c, final Object... objs) {
    ReflectionCode refl = new ObjectContractReflectionCode(c, objs);
    Timer timer = new Timer();
    timer.startTiming();
    Throwable t = ReflectionExecutor.executeReflectionCode(refl, System.out);
    timer.stopTiming();

    if (refl.getExceptionThrown() != null) {
      return new ExceptionalExecution(refl.getExceptionThrown(), timer.getTimeElapsedMillis());
    }
    if (t != null) {
      return new ExceptionalExecution(t, timer.getTimeElapsedMillis());
    }
    return new NormalExecution(refl.getReturnValue(), timer.getTimeElapsedMillis());
  }

  /**
   * Replace dummy variables such as "x0" in the code by their real names.
   *
   * @param str the contract code as a string with dummy variables
   * @param vars list of {@link randoop.sequence.Variable Variable} objects
   * @return the contract code with actual variable names substituted for dummy names
   */
  public static String localizeContractCode(String str, Variable... vars) {
    for (int i = 0; i < vars.length; i++) {
      // See documentation for ObjectContract.toCommentString().
      String pattern = "\\bx" + i + "\\b";
      str = str.replaceAll(pattern, vars[i].getName());
    }
    return str;
  }
}
