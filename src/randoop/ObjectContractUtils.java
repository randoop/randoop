package randoop;

import randoop.util.ReflectionCode;
import randoop.util.ReflectionExecutor;
import randoop.util.Timer;

/**
 * Utility methods for safely executing and printing {@link ObjectContract} code.
 */
public class ObjectContractUtils {

  /**
   * Executes the given contract via reflection.
   */
  public static ExecutionOutcome execute(final ObjectContract c, final Object... objs) {
    ReflectionCode refl = new ReflectionCode() {
      private Object result;
      private Throwable exception;

      @Override
      public Throwable getExceptionThrown() {
        return exception;
      }

      @Override
      public Object getReturnVariable() {
        return result;
      }

      @Override
      protected void runReflectionCodeRaw() {
        try {
          result = c.evaluate(objs);
        } catch (Throwable e) {
          exception = e;
        } finally {
          setRunAlready();
        }
      }
    };
    Timer timer = new Timer();
    timer.startTiming();
    Throwable t = ReflectionExecutor.executeReflectionCode(refl, System.out);
    timer.stopTiming();
    if (t != null || refl.getExceptionThrown() != null) {
      return new ExceptionalExecution(refl.getExceptionThrown(), timer
          .getTimeElapsedMillis());
    }
    return new NormalExecution(refl.getReturnVariable(), timer
        .getTimeElapsedMillis());
  }
  
  public static String localizeContractCode(String str, Variable... vars) {
    for (int i = 0 ; i < vars.length ; i++) {
      // See documentation for Expression.toCommentString().
      str = str.replaceAll("x" + i, vars[i].getName());
    }
    return str; 
  }
}
