package randoop.test;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * ErrorRevealingChecks represent failing checks for a particular test sequence.
 * Each check may refer to different variables defined in the sequence, so can
 * only be assumed to be valid at the end of the sequence. Note that there are
 * no expected exceptions in error revealing tests, and so there should be no
 * {@link ExceptionCheck} objects.
 */
public class ErrorRevealingChecks implements TestChecks {

  private Set<Check> checks;

  /**
   * Create an empty set of error checks.
   */
  ErrorRevealingChecks() {
    this.checks = new LinkedHashSet<>();
  }

  /**
   * {@inheritDoc}
   *
   * @return count of error revealing checks
   */
  @Override
  public int count() {
    return checks.size();
  }

  /**
   * {@inheritDoc}
   *
   * @return all checks with false, indicating all are failing
   */
  @Override
  public Map<Check, Boolean> get() {
    Map<Check, Boolean> result = new LinkedHashMap<>();
    if (hasChecks()) {
      for (Check ck : checks) {
        result.put(ck, false);
      }
    }
    return result;
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
   * @throws Error
   *           if {@code check} is an exception check
   */
  @Override
  public void add(Check check) {

    if (check instanceof ExceptionCheck) {
      String msg = "No expected exceptions in error-revealing tests";
      throw new Error(msg);
    }

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
  public TestChecks commonChecks(TestChecks testChecks) {
    if (!(testChecks instanceof ErrorRevealingChecks)) {
      throw new IllegalArgumentException("Must compare with ErrorRevealingChecks");
    }
    ErrorRevealingChecks erc = (ErrorRevealingChecks) testChecks;
    TestChecks common = new ErrorRevealingChecks();
    for (Check ck : checks) {
      if (erc.checks.contains(ck)) {
        common.add(ck);
      }
    }
    return common;
  }

  /**
   * {@inheritDoc} Returns false because error checks are not considered
   * invalid.
   *
   * @return false, always
   */
  @Override
  public boolean hasInvalidBehavior() {
    return false;
  }
}
