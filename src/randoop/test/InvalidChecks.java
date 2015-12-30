package randoop.test;

import java.util.LinkedHashMap;
import java.util.Map;

import randoop.Check;
import randoop.ExceptionCheck;

/**
 * Implements a set of checks capturing invalid behavior in a sequence.
 */
public class InvalidChecks implements TestChecks {

  private ExceptionCheck exceptionCheck;

  @Override
  public int count() {
    int result = 0;
    if (exceptionCheck != null) {
      result = 1;
    }
    return result;
  }

  @Override
  public Map<Check, Boolean> get() {
    Map<Check,Boolean> mp = new LinkedHashMap<Check,Boolean>();
    if (exceptionCheck != null) {
      mp.put(exceptionCheck, false);
    }
    return mp;
  }

  @Override
  public boolean hasChecks() {
    return exceptionCheck != null;
  }

  @Override
  public boolean hasErrorBehavior() {
    return false;
  }

  @Override
  public ExceptionCheck getExceptionCheck() {
    return exceptionCheck;
  }

  @Override
  public void add(Check check) {
    if (check instanceof ExceptionCheck) {
      exceptionCheck = (ExceptionCheck)check;
    }
  }

  @Override
  public TestChecks commonChecks(TestChecks checks) {
    if (! (checks instanceof InvalidChecks)) {
      throw new IllegalArgumentException("Must compare with an InvalidChecks object");
    }
    InvalidChecks ic = (InvalidChecks)checks;
    TestChecks common = new InvalidChecks();
    if (this.exceptionCheck != null && exceptionCheck.equals(ic.exceptionCheck)) {
      common.add(exceptionCheck);
    }
    return common;
  }

  @Override
  public boolean hasInvalidBehavior() {
    return exceptionCheck != null;
  }

}
