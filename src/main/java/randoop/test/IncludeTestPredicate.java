package randoop.test;

import java.util.function.Predicate;
import java.util.regex.Pattern;
import randoop.sequence.ExecutableSequence;

/**
 * Returns true if the test uses a variable (or method) defined in a class that matches the given
 * regular expression.
 */
public class IncludeTestPredicate implements Predicate<ExecutableSequence> {

  /**
   * A pattern that matches classes whose use is required. The predicate returns true if a class
   * matching this pattern is used.
   */
  private final Pattern testClasses;

  /**
   * Creates an IncludeTestPredicate.
   *
   * @param testClasses a pattern that matches classes whose uses to require
   */
  public IncludeTestPredicate(Pattern testClasses) {
    this.testClasses = testClasses;
  }

  /**
   * {@inheritDoc}
   *
   * @return true if the sequence uses a member of a class that matches the regular expression,
   *     false otherwise
   */
  @Override
  public boolean test(ExecutableSequence eseq) {
    return eseq.sequence.hasUseOfMatchingClass(testClasses);
  }
}
