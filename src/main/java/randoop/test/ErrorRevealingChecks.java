package randoop.test;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * ErrorRevealingChecks represent failing checks for a particular test sequence. Each check may
 * refer to different variables defined in the sequence, so can only be assumed to be valid at the
 * end of the sequence. Note that there are no expected exceptions in error-revealing tests, and so
 * there should be no {@link ExceptionCheck} objects.
 */
public class ErrorRevealingChecks implements TestChecks<ErrorRevealingChecks> {

  /** An empty, immutable set of error-revealing checks. */
  public static final ErrorRevealingChecks EMPTY;

  static {
    EMPTY = new ErrorRevealingChecks();
    EMPTY.checks = Collections.emptySet(); // make immutable
  }

  private Set<Check> checks;

  /** Create an empty set of error checks. */
  public ErrorRevealingChecks() {
    this.checks = new LinkedHashSet<>();
  }

  /**
   * Create a singleton set of error checks.
   *
   * @param check the check to put in the newly-created singleton set
   */
  public ErrorRevealingChecks(Check check) {
    validateCheck(check);
    this.checks = Collections.singleton(check);
  }

  /**
   * Throw an exception if {@code check} is not acceptable for this class.
   *
   * @param check the check that a client is trying to insert into this
   */
  private static void validateCheck(Check check) {
    if ((check instanceof ExceptionCheck) && !(check instanceof ExpectedExceptionCheck)) {
      throw new Error(
          "No expected exceptions in error-revealing tests (class "
              + check.getClass()
              + "): "
              + check);
    }
  }

  @Override
  public int count() {
    return checks.size();
  }

  /**
   * {@inheritDoc}
   *
   * @return all checks, with each mapped to false, indicating it is failing
   */
  @Override
  public Set<Check> checks() {
    return checks;
  }

  /**
   * {@inheritDoc}
   *
   * @return true if not empty
   */
  @Override
  public boolean hasChecks() {
    return !checks.isEmpty();
  }

  /**
   * {@inheritDoc}
   *
   * @return true if there are any error revealing checks (not empty)
   */
  @Override
  public boolean hasErrorBehavior() {
    return hasChecks();
  }

  /**
   * {@inheritDoc}
   *
   * @return null, since no expected exceptions in error-revealing tests
   */
  @Override
  public ExceptionCheck getExceptionCheck() {
    return null;
  }

  /**
   * {@inheritDoc}
   *
   * @throws Error if {@code check} is an exception check
   */
  @Override
  public void add(Check check) {
    validateCheck(check);
    checks.add(check);
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof ErrorRevealingChecks)) {
      return false;
    }
    ErrorRevealingChecks cks = (ErrorRevealingChecks) obj;
    return this.checks.equals(cks.checks);
  }

  @Override
  public int hashCode() {
    return Objects.hash(checks);
  }

  @Override
  public ErrorRevealingChecks commonChecks(ErrorRevealingChecks other) {
    ErrorRevealingChecks common = new ErrorRevealingChecks();
    for (Check ck : checks) {
      if (other.checks.contains(ck)) {
        common.add(ck);
      }
    }
    return common;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Returns false because error checks are not considered invalid.
   *
   * @return false, always
   */
  @Override
  public boolean hasInvalidBehavior() {
    return false;
  }

  @Override
  public String toString() {
    return this.getClass() + " of size " + checks.size() + ": " + checks.toString();
  }
}
