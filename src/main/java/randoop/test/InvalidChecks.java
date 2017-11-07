package randoop.test;

import java.util.Collections;
import java.util.Set;
import randoop.BugInRandoopException;

/**
 * An empty or singleton set. It contains at most one InvalidExceptionCheck, which captures invalid
 * behavior in a sequence.
 */
public class InvalidChecks implements TestChecks<InvalidChecks> {

  /** An empty set of erorr-revealing checks. */
  public static final InvalidChecks EMPTY = new InvalidChecks();

  private InvalidExceptionCheck check;

  /** Create an empty set of invalid checks. */
  public InvalidChecks() {}

  /** Create a singleton set of invalid checks. */
  public InvalidChecks(InvalidExceptionCheck check) {
    add(check);
  }

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
      return Collections.<Check>singleton(check);
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
  public InvalidExceptionCheck getExceptionCheck() {
    return check;
  }

  @Override
  public void add(Check check) {
    if (this == EMPTY) {
      throw new BugInRandoopException("Don't add to InvalidChecks.EMPTY");
    }
    if (this.check != null) {
      throw new BugInRandoopException(
          String.format("add(%s) when InvalidChecks already contains %s", check, this.check));
    }
    if (!(check instanceof InvalidExceptionCheck)) {
      throw new Error("Expected InvalidExceptionCheck: " + check);
    }
    this.check = (InvalidExceptionCheck) check;
  }

  @Override
  public InvalidChecks commonChecks(InvalidChecks other) {
    InvalidChecks common = new InvalidChecks();
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
