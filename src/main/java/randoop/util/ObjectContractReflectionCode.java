package randoop.util;

import java.util.Arrays;
import org.plumelib.util.ArraysPlume;
import randoop.contract.ObjectContract;

public final class ObjectContractReflectionCode extends ReflectionCode {

  final ObjectContract c;
  final Object[] objs;

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
