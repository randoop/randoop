package randoop.util;

import java.util.Arrays;
import org.plumelib.util.ArraysPlume;
import randoop.contract.ObjectContract;

/** A contract about an object contract. */
public final class ObjectContractReflectionCode extends ReflectionCode {

  /** The contract to evaluate. */
  final ObjectContract c;

  /** The values that the contract is evaluated on. */
  final Object[] objs;

  /**
   * Creates an ObjectContractReflectionCode that evaluates the given contract on the given values.
   *
   * @param c the contract to evaluate
   * @param objs the values that the contract is evaluated on
   */
  @SuppressWarnings("PMD.ArrayIsStoredDirectly")
  public ObjectContractReflectionCode(final ObjectContract c, final Object... objs) {
    this.c = c;
    this.objs = objs;
  }

  @Override
  protected void runReflectionCodeRaw() {
    try {
      retval = c.evaluate(objs); // always a boolean value (true or false)
    } catch (Throwable e) {
      exceptionThrown = e;
    }
  }

  @Override
  public String toString() {
    return "Check of ObjectContract "
        + c
        + " args: "
        + Arrays.toString(objs)
        + ", arg types: "
        + Arrays.toString(
            ArraysPlume.mapArray(x -> x == null ? null : x.getClass(), objs, Class.class))
        + " "
        + status();
  }
}
