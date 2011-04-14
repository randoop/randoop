package randoop.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import randoop.main.GenInputsAbstract;

/**
 * This is used to wrap a constructor together with its parameters, ready for execution.
 * Can be run only once.
 */
public final class ConstructorReflectionCode extends ReflectionCode {
  private final Constructor<?> constructor;
  private final Object[] inputs;
  private Object retval;
  private Throwable exceptionThrown;

  public ConstructorReflectionCode(Constructor<?> constructor, Object[] inputs) {
    if (constructor == null) throw new IllegalArgumentException("constrcutor is null");
    if (inputs == null) throw new IllegalArgumentException("inputs is null");
    this.constructor = constructor;
    this.inputs = inputs;
    checkRep();
  }

  private void checkRep() {
    if (!GenInputsAbstract.debug_checks)
      return;
    String error = Reflection.checkArgumentTypes(inputs, constructor.getParameterTypes(), constructor);
    if (error != null)
      throw new IllegalArgumentException(error);
  }

  @Override
  public void runReflectionCodeRaw() throws InstantiationException, IllegalAccessException, InvocationTargetException {

    if (hasRunAlready())
      throw new NotCaughtIllegalStateException("cannot run this twice " + this);

    this.setRunAlready();

    if (!this.constructor.isAccessible()) {
      this.constructor.setAccessible(true);
      Log.logLine("not accessible:" + this.constructor);
      // TODO something is bizzare - it seems that a public method can be not-accessible sometimes. RatNum(int,int)
      // TODO you cannot just throw the exception below - because no sequences will be created in the randoop.experiments.
      // throw new IllegalStateException("Not accessible: " + this.constructor);
    }

    try{
      this.retval = this.constructor.newInstance(this.inputs);
    } catch (InvocationTargetException e) {
      this.exceptionThrown= e.getCause();
      throw e;
    } finally {
      if (retval != null && exceptionThrown != null)
        throw new NotCaughtIllegalStateException("cannot have both retval and exception not null");
    }
  }

  @Override
  public Object getReturnVariable() {
    if (! hasRunAlready())
      throw new IllegalStateException("run first, then ask");
    return retval;
  }

  @Override
  public Throwable getExceptionThrown() {
    if (! hasRunAlready())
      throw new IllegalStateException("run first, then ask");
    return exceptionThrown;
  }

  public Constructor<?> getConstructor() {
    return this.constructor;
  }

  public Object[] getInputs() {
    return this.inputs.clone();// be defensive
  }

  @Override
  public String toString() {
    String ret= "Call to " + constructor + " args:" + Arrays.toString(inputs);
    if (hasRunAlready())
      return ret + " not run yet";
    else if (exceptionThrown == null)
      return ret + " returned:" + ret;
    else
      return ret + " threw:" + exceptionThrown;
  }
}
