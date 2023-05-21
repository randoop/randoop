package randoop.test;

import java.util.function.Predicate;
import java.util.regex.Pattern;
import randoop.sequence.ExecutableSequence;

/**
 * Returns true if the test uses a variable (or method) defined in a class that matches the given
 * regular expression.
 */
public class IncludeTestPredicate implements Predicate<ExecutableSequence> {

  /** Classes whose uses to require. Predicate returns true if one of these is used. */
  private Pattern testClasses;

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
