package randoop;

import java.util.List;

import randoop.util.ReflectionCode;
import randoop.util.ReflectionExecutor;
import randoop.util.Timer;

/**
 * Contains utility methods for printing expressions as code, executing them,
 * etc.
 */
public class ExpressionUtils {

  /**
   * Returns a string that can be used as Java source code to represent this
   * expression.
   */
  public static String toCodeString(Class<? extends Expression> expression,
      List<Variable> vars) {
    StringBuilder b = new StringBuilder();
    b.append("new ");
    b.append(expression.getName());
    b.append("().evaluate(");
    for (int i = 0 ; i < vars.size() ; i++) {
      if (i > 0)
        b.append(",");
      b.append(vars.get(i).toString());
    }
    b.append(")");
    return b.toString();
  }

  /**
   * Executes this expression via Java reflection.
   */
  public static ExecutionOutcome execute(final Expression expr,
      final Object... objs) {
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
          result = expr.evaluate(objs);
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
  
  public static String localizeExpressionComment(
      Class<? extends Expression> expression, List<Variable> vars) {
    Expression exp = null;
    try {
      exp = expression.newInstance();
    } catch (Exception e) {
      // Every expression classs must declare a 0-argument constructor.
      throw new Error(e);
    }
    
    String str = exp.toCommentString();
    for (int i = 0 ; i < exp.getArity() ; i++) {
      
      // See documentation for Expression.toCommentString().
      str = str.replaceAll("x" + i, vars.get(i).getName());
    }
    return str;
  }

  public static String localizeExpressionCode(
      Class<? extends Expression> expression, List<Variable> vars) {
    Expression exp = null;
    try {
      exp = expression.newInstance();
    } catch (Exception e) {
      // Every expression classs must declare a 0-argument constructor.
      throw new Error(e);
    }
    
    String str = exp.toCodeString();
    for (int i = 0 ; i < exp.getArity() ; i++) {
      
      // See documentation for Expression.toCommentString().
      str = str.replaceAll("x" + i, vars.get(i).getName());
    }
    return str;
  }


}
