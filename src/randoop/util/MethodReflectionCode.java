package randoop.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import randoop.main.GenInputsAbstract;




/**
 * This is used to wrap a method together with its parameters, ready for execution.
 * Can be run only once.
 */
public final class MethodReflectionCode extends ReflectionCode {

  private final Method method;
  private final Object receiver;
  private final Object[] inputs;
  private Object retval;
  private Throwable exceptionThrown;

  /*
   * receiver is ok to be null - will cause NPE on invocation
   */
  public MethodReflectionCode(Method method, Object receiver, Object[] inputs) {
    if (method == null) throw new IllegalArgumentException("method is null");
    if (inputs == null) throw new IllegalArgumentException("inputs is null");
    this.receiver = receiver;
    this.method = method;
    this.inputs = inputs;
    checkRep();
  }

  private void checkRep() {
    if (!GenInputsAbstract.debug_checks)
      return;
    String error= Reflection.checkArgumentTypes(inputs, method.getParameterTypes(), method);
    if (error != null)
      throw new IllegalArgumentException(error);
    if (Modifier.isStatic(this.method.getModifiers())) {
      if (receiver != null)
        throw new IllegalArgumentException("receiver must be null for static method.");
    } else {
      if(! Reflection.canBePassedAsArgument(receiver, method.getDeclaringClass()))
        throw new IllegalArgumentException("method " + method + " cannot be invoked on " + receiver);
    }
    // TODO check that the lookup starting at receiver.getClass<?> will result
    // in method
  }

  @Override
  public void runReflectionCodeRaw() throws IllegalAccessException, InvocationTargetException {

    if (hasRunAlready())
      throw new NotCaughtIllegalStateException("cannot run this twice " + this);

    this.setRunAlready();

    if (!this.method.isAccessible()) {
      this.method.setAccessible(true);
      Log.logLine("not accessible:" + this.method);
      // TODO something is bizzare - it seems that a public method can be
      // not-accessible sometimes. RatNum(int,int)
      // TODO you cannot just throw the exception below - because no
      // sequences will be created in the randoop.experiments.
      // throw new IllegalStateException("Not accessible: " + this.meth);
    }

    try {
      assert this.method != null;
      this.retval = this.method.invoke(this.receiver, this.inputs);

      if (receiver == null && isInstanceMethod())
        throw new NotCaughtIllegalStateException("receiver was null - expected NPE from call to: " + method);
    } catch (NullPointerException e) {
      this.exceptionThrown= e;
      throw e;
    } catch (InvocationTargetException e) {
      this.exceptionThrown= e.getCause();
      throw e;
    } finally {
      if (retval != null && exceptionThrown != null)
        throw new NotCaughtIllegalStateException("cannot have both retval and exception not null");
    }
  }

  private boolean isInstanceMethod() {
    return ! Modifier.isStatic(method.getModifiers());
  }

  @Override
  public Object getReturnVariable() {
    if (! hasRunAlready())
      throw new IllegalStateException("run first, then ask");
    if (receiver == null && retval != null && isInstanceMethod())
      throw new IllegalStateException("receiver was null - expected NPE from call to: " + method);
    return retval;
  }

  @Override
  public Throwable getExceptionThrown() {
    if (! hasRunAlready())
      throw new IllegalStateException("run first, then ask");
    if (receiver == null && !(exceptionThrown instanceof NullPointerException) && isInstanceMethod())
      throw new IllegalStateException("receiver was null - expected NPE from call to: " + method);
    return exceptionThrown;
  }

  public Method getMethod() {
    return this.method;
  }

  public Object getReceiver() {
    return this.receiver;
  }

  public Object[] getInputs() {
    return this.inputs.clone(); // be defensive
  }

  @Override
  public String toString() {
    String ret= "Call to " + method + " receiver:" + receiver + " args:" + Arrays.toString(inputs);
    if (! hasRunAlready())
      return ret + " not run yet";
    else if (exceptionThrown == null)
      return ret + " returned:" + retval;
    else
      return ret + " threw:" + exceptionThrown;
  }
}
