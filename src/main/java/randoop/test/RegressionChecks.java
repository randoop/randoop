package randoop.test;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.Nullable;

/** A set of checks, including at most one ExceptionCheck. */
public class RegressionChecks implements TestChecks<RegressionChecks> {

  /** An empty, immutable set of regression checks. */
  public static RegressionChecks EMPTY = new RegressionChecks();

  private Set<Check> checks;
  private @Nullable ExceptionCheck exceptionCheck;

  /** Create an empty set of regression checks. */
  public RegressionChecks() {
    this.checks = new LinkedHashSet<>();
    this.exceptionCheck = null;
  }

  /**
   * Create a singleton set of regression checks.
   *
   * @param check the check to put in the newly-created singleton set
   */
  public RegressionChecks(Check check) {
    if (check instanceof ExceptionCheck) {
      this.checks = Collections.emptySet();
      this.exceptionCheck = (ExceptionCheck) check;
    } else {
      this.checks = Collections.singleton(check);
      this.exceptionCheck = null;
    }
  }

  /**
   * Create a new RegressionCheck.
   *
   * @param checks the non-exception checks
   * @param exceptionCheck the exception check, or null
   */
  public RegressionChecks(Collection<Check> checks, @Nullable ExceptionCheck exceptionCheck) {
    this.checks = Collections.unmodifiableSet(new LinkedHashSet<>(checks));
    this.exceptionCheck = exceptionCheck;

    if (checks.stream().anyMatch(ExceptionCheck.class::isInstance)) {
      throw new IllegalArgumentException("checks must not contain ExceptionCheck instances");
    }
  }

  @Override
  public int count() {
    int result = checks.size();
    if (exceptionCheck != null) {
      result++;
    }
    return result;
  }

  /**
   * {@inheritDoc}
   *
   * @return map of non-exception checks in this object. each mapped to true, indicating it is
   *     passing.
   */
  @Override
  public Set<Check> checks() {
    return checks;
  }

  /**
   * {@inheritDoc}
   *
   * @return true if there are regression checks or an expected exception, false otherwise
   */
  @Override
  public boolean hasChecks() {
    return (!checks.isEmpty() || exceptionCheck != null);
  }

  /**
   * {@inheritDoc}
   *
   * @return false, since all regression checks are passing
   */
  @Override
  public boolean hasErrorBehavior() {
    return false;
  }

  @Override
  public ExceptionCheck getExceptionCheck() {
    return exceptionCheck;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof RegressionChecks)) {
      return false;
    }
    RegressionChecks cks = (RegressionChecks) obj;
    if (!checks.equals(cks.checks)) {
      return false;
    }
    if (exceptionCheck == null) {
      return (cks.exceptionCheck == null);
    }
    return cks.exceptionCheck != null && exceptionCheck.equals(cks.exceptionCheck);
  }

  @Override
  public int hashCode() {
    return Objects.hash(checks, exceptionCheck);
  }

  /**
   * {@inheritDoc}
   *
   * <p>Returns false because regression checks are not invalid.
   *
   * @return false, always
   */
  @Override
  public boolean hasInvalidBehavior() {
    return false;
  }
}
