package randoop.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

/** Wraps a constructor together with its arguments, ready for execution. Can be run only once. */
public final class ConstructorReflectionCode extends ReflectionCode {
  private final Constructor<?> constructor;
  private final Object[] inputs;

  public ConstructorReflectionCode(Constructor<?> constructor, Object[] inputs) {
    if (constructor == null) {
      throw new IllegalArgumentException("constructor is null");
    }
    if (inputs == null) {
      throw new IllegalArgumentException("inputs is null");
    }
    this.constructor = constructor;
    this.inputs = inputs;

    if (!this.constructor.isAccessible()) {
      this.constructor.setAccessible(true);
      Log.logLine("not accessible: " + this.constructor);
      // TODO something is bizarre - it seems that a public method can be
      // not-accessible sometimes. RatNum(int,int)
      // TODO you cannot just throw the exception below - because no sequences
      // will be created in the randoop.experiments.
      // throw new IllegalStateException("Not accessible: " + this.constructor);
    }
  }

  @SuppressWarnings("Finally")
  @Override
  public void runReflectionCodeRaw()
      throws InstantiationException, IllegalAccessException, InvocationTargetException {
    try {
      this.retval = this.constructor.newInstance(this.inputs);
    } catch (InvocationTargetException e) {
      this.exceptionThrown = e.getCause();
      throw e;
    }
  }

  @Override
  public String toString() {
    String ret = "Call to " + constructor + ", args: " + Arrays.toString(inputs);
    if (hasRunAlready()) {
      return ret + " not run yet";
    } else if (exceptionThrown == null) {
      return ret + " returned: " + ret;
    } else {
      return ret + " threw: " + exceptionThrown;
    }
  }
}
