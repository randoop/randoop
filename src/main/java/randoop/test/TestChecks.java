package randoop.test;

import java.util.Set;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A TestChecks reprents a set of Checks, and possibly a single ExceptionCheck.
 *
 * @see RegressionChecks
 * @see ErrorRevealingChecks
 * @see InvalidChecks
 */
public interface TestChecks<T extends TestChecks<T>> {

  /**
   * Return the number of checks in this test.
   *
   * @return the count of checks in this object
   */
  int count();

  /**
   * Get all non-exception checks and whether they are passing for this object.
   *
   * @return all checks with passing status
   */
  Set<Check> checks();

  /**
   * Add a check to this set.
   *
   * @param ck the check object to add to this set of checks
   */
  void add(Check ck);

  /**
   * Indicates whether this object has checks.
   *
   * @return true if this object has checks, false otherwise
   */
  boolean hasChecks();

  /**
   * Indicates whether this set of checks contains any invalid behaviors.
   *
   * @return true when this contains checks for invalid behavior, false otherwise
   */
  boolean hasInvalidBehavior();

  /**
   * Indicate whether this object has any failing checks. (This is essentially asking whether this
   * is an error revealing test.)
   *
   * @return true if this object has failing checks, false otherwise
   */
  boolean hasErrorBehavior();

  /**
   * Return the exception check in this object if there is one.
   *
   * @return the expected exception check, null otherwise
   */
  @Nullable ExceptionCheck getExceptionCheck();

  /**
   * Returns the intersection of checks in this set and another set.
   *
   * @param other the {@code TestChecks} to compare with
   * @return the checks common to this set of checks and those in {@code other}
   */
  T commonChecks(T other);
}
