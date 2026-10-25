package randoop.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import org.plumelib.util.ArraysPlume;

/** Wraps a constructor together with its arguments, ready for execution. Can be run only once. */
public final class ConstructorReflectionCode extends ReflectionCode {

  /** The constructor to be called. */
  private final Constructor<?> constructor;

  /**
   * The arguments that the constructor is applied to. If an inner class constructor has a receiver,
   * it is the first element of this array.
   */
  private final Object[] inputs;

  /**
   * Create a new ConstructorReflectionCode to represent a constructor invocation.
   *
   * @param constructor the constructor to be called
   * @param inputs the arguments that the constructor is applied to. If an inner class constructor
   *     has a receiver, it is the first element of this array.
   */
  @SuppressWarnings("deprecation") // AccessibleObject.isAccessible() has no replacement in Java 8.
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
      Log.logPrintf("not accessible: %s%n", this.constructor);
      // TODO something is bizarre - it seems that a public method can be
      // not-accessible sometimes. RatNum(int,int)
      // TODO you cannot just throw the exception below - because no sequences
      // will be created in the randoop.experiments.
      // throw new IllegalStateException("Not accessible: " + this.constructor);
    }
  }

  @SuppressWarnings({
    "Finally",
    "signedness:assignment" // reflection
  })
  @Override
  public void runReflectionCodeRaw() {
    try {
      this.retval = this.constructor.newInstance(this.inputs);
    } catch (InvocationTargetException e) {
      // The underlying constructor threw an exception
      this.exceptionThrown = e.getCause();
      // new Error(
      //     String.format(
      //         "Failure in newInstance: constructor=%s, args=%s%n",
      //         this.constructor, Arrays.toString(this.inputs)),
      //     e);
    } catch (Throwable e) {
      // Any other exception indicates Randoop should not have called the constructor
      throw new ReflectionCodeException(
          String.format(
              "Failure in newInstance: constructor=%s, args=%s%n",
              this.constructor, Arrays.toString(this.inputs)),
          e);
    }
  }

  @Override
  public String toString() {
    return getClass().getSimpleName()
        + ": "
        + constructor
        + ", args: "
        + Arrays.toString(inputs)
        + ", arg types: "
        + Arrays.toString(
            ArraysPlume.mapArray(x -> x == null ? null : x.getClass(), inputs, Class.class))
        + " "
        + status();
  }
}
