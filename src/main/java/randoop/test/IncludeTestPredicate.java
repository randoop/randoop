package randoop.test;

import java.util.function.Predicate;
import java.util.regex.Pattern;
import randoop.sequence.ExecutableSequence;

/**
 * A test predicate that checks for the occurrence of variables that match the given regular
 * expression.
 */
public class IncludeTestPredicate implements Predicate<ExecutableSequence> {

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
