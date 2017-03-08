package randoop.test;

import java.util.Map;

/**
 * TestChecks represents the checks for a particular test sequence. Can either be all passing tests
 * for a regression test, or all failing tests for an error-revealing test.
 *
 * @see RegressionChecks
 * @see ErrorRevealingChecks
 */
public interface TestChecks {

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
  Map<Check, Boolean> get();

  /**
   * Indicates whether this set of checks corresponds to valid behaviors.
   *
   * @return true when has checks for invalid behavior, false otherwise
   */
  boolean hasInvalidBehavior();

  /**
   * Indicates whether this object has checks.
   *
   * @return true if this object has checks, false otherwise
   */
  boolean hasChecks();

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
  ExceptionCheck getExceptionCheck();

  /**
   * Add a check to this sequence.
   *
   * @param ck the check object to add to this set of checks
   */
  void add(Check ck);

  /**
   * Returns the consensus checks for two sets of checks. Refuses to compare passing with failing
   * checks.
   *
   * @param checks the {@code TestChecks} to compare with.
   * @return the checks common to this set of checks and those in {@code checks}
   */
  TestChecks commonChecks(TestChecks checks);
}
