package randoop.test;

import java.util.Collections;
import java.util.Set;
import randoop.main.RandoopBug;

/**
 * An empty or singleton set. It contains at most one InvalidExceptionCheck or InvalidValueCheck,
 * which captures invalid behavior in a sequence.
 */
public class InvalidChecks implements TestChecks<InvalidChecks> {

  /** An empty, immutable set of invalid checks. */
  public static final InvalidChecks EMPTY = new InvalidChecks();

  // Either an InvalidExceptionCheck or an InvalidValueCheck.
  private Check check;

  /** Create an empty, mutable set of invalid checks. */
  public InvalidChecks() {}

  /**
   * Create a singleton set of invalid checks.
   *
   * @param check the sole member of the newly-created singleton set
   */
  public InvalidChecks(Check check) {
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
  public ExceptionCheck getExceptionCheck() {
    // TODO is this right?  The result might be an InvalidValueCheck
    if (check instanceof InvalidExceptionCheck) {
      return (InvalidExceptionCheck) check;
    } else {
      return null;
    }
  }

  @Override
  public void add(Check check) {
    if (this == EMPTY) {
      throw new RandoopBug("Don't add to InvalidChecks.EMPTY");
    }
    if (this.check != null) {
      throw new RandoopBug(
          String.format("add(%s) when InvalidChecks already contains %s", check, this.check));
    }
    if (!((check instanceof InvalidExceptionCheck) || (check instanceof InvalidValueCheck))) {
      throw new Error("Expected Invalid{Exception,Value}Check, got " + check);
    }
    this.check = check;
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
