package randoop.test;

import java.util.Collections;
import java.util.Set;
import randoop.BugInRandoopException;

/**
 * An empty or singleton set. It contains at most one check, which captures invalid behavior in a
 * sequence.
 */
public class InvalidCheck implements TestChecks<InvalidCheck> {

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
  public Set<Check> checks() {
    if (check != null) {
      return Collections.singleton(check);
    } else {
      return Collections.emptySet();
    }
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
    if (this.check != null) {
      throw new BugInRandoopException(
          String.format("add(%s) when InvalidCheck already contains %s", check, this.check));
    }
    this.check = check;
  }

  @Override
  public InvalidCheck commonChecks(InvalidCheck other) {
    InvalidCheck common = new InvalidCheck();
    if (this.check != null && check.equals(other.check)) {
      common.add(check);
    }
    return common;
  }

  @Override
  public boolean hasInvalidBehavior() {
    return check != null;
  }
}
