package randoop.test;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import randoop.test.Check;
import randoop.test.ExceptionCheck;

public class RegressionChecks implements TestChecks {

  private Set<Check> checks;
  private ExceptionCheck exceptionCheck;

  public RegressionChecks() {
    this.checks = new LinkedHashSet<>();
    this.exceptionCheck = null;
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
   * Adds the given check to the sequence. Only one {@code ExceptionCheck} is
   * allowed, and attempting to add a second check of this type will result in
   * an {@code IllegalArgumentException}
   *
   * @throws IllegalArgumentException
   *           If the given check's class is {@code ExceptionCheck} and there is
   *           already an check of this class at the give index.
   */
  @Override
  public void add(Check check) {
    if (check instanceof ExceptionCheck) {
      if (exceptionCheck != null) {
        throw new IllegalArgumentException(
            "Sequence already has a check"
                + " of type "
                + exceptionCheck.getClass().getCanonicalName());
      }
      exceptionCheck = (ExceptionCheck) check;
    } else {
      checks.add(check);
    }
  }

  /**
   * {@inheritDoc}
   *
   * @return map of non-exception checks in this object, all of which are
   *         passing
   */
  @Override
  public Map<Check, Boolean> get() {
    Map<Check, Boolean> mp = new LinkedHashMap<>();
    for (Check ck : checks) {
      mp.put(ck, true);
    }
    return mp;
  }

  /**
   * {@inheritDoc}
   *
   * @return true if there are regression checks or an expected exception, false
   *         otherwise
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
  public boolean equals(Object obj) {
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
    if (cks.exceptionCheck == null) {
      return false;
    }
    return exceptionCheck.equals(cks.exceptionCheck);
  }

  @Override
  public int hashCode() {
    return Objects.hash(checks, exceptionCheck);
  }

  @Override
  public TestChecks commonChecks(TestChecks testChecks) {
    if (!(testChecks instanceof RegressionChecks)) {
      throw new IllegalArgumentException("Must compare with a RegressionChecks object");
    }
    RegressionChecks common = new RegressionChecks();
    RegressionChecks rc = (RegressionChecks) testChecks;
    for (Check ck : checks) {
      if (rc.checks.contains(ck)) {
        common.add(ck);
      }
    }
    if (exceptionCheck.equals(rc.exceptionCheck)) {
      common.add(exceptionCheck);
    }
    return common;
  }

  /**
   * {@inheritDoc} Returns false because regression checks are not invalid.
   *
   * @return false, always
   */
  @Override
  public boolean hasInvalidBehavior() {
    return false;
  }
}
