package randoop.util;

import randoop.contract.ObjectContract;

/** Reflection code that evaluates an {@link ObjectContract} on given values. */
public final class ObjectContractReflectionCode extends ReflectionCode {

  /** The contract to evaluate. */
  private final ObjectContract c;

  /** The values that the contract is evaluated on. */
  private final Object[] objs;

  /**
   * Creates an ObjectContractReflectionCode that evaluates the given contract on the given values.
   *
   * @param c the contract to evaluate
   * @param objs the values that the contract is evaluated on
   */
  @SuppressWarnings("PMD.ArrayIsStoredDirectly")
  public ObjectContractReflectionCode(final ObjectContract c, final Object... objs) {
    super();
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
        + Util.elementsToStringAndClassSafely(objs)
        + " "
        + status();
  }
}
