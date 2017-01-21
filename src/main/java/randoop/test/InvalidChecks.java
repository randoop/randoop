package randoop.test;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Implements a set of checks capturing invalid behavior in a sequence.
 */
public class InvalidChecks implements TestChecks {

  private Check check;

  @Override
  public int count() {
    int result = 0;
    if (check != null) {
      result = 1;
    }
    return result;
  }

  @Override
  public Map<Check, Boolean> get() {
    Map<Check, Boolean> mp = new LinkedHashMap<>();
    if (check != null) {
      mp.put(check, false);
    }
    return mp;
  }

  @Override
  public boolean hasChecks() {
    return check != null;
  }

  @Override
  public boolean hasErrorBehavior() {
    return false;
  }

  @Override
  public ExceptionCheck getExceptionCheck() {
    if (check instanceof ExceptionCheck) {
      return (ExceptionCheck) check;
    }
    return null;
  }

  @Override
  public void add(Check check) {
    this.check = check;
  }

  @Override
  public TestChecks commonChecks(TestChecks checks) {
    if (!(checks instanceof InvalidChecks)) {
      throw new IllegalArgumentException("Must compare with an InvalidChecks object");
    }
    InvalidChecks ic = (InvalidChecks) checks;
    TestChecks common = new InvalidChecks();
    if (this.check != null && check.equals(ic.check)) {
      common.add(check);
    }
    return common;
  }

  @Override
  public boolean hasInvalidBehavior() {
    return check != null;
  }
}
