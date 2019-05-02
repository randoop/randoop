package randoop.test;

import java.util.Set;
import java.util.function.Predicate;
import randoop.sequence.ExecutableSequence;

/**
 * Checks whether the most recent execution of the {@link ExecutableSequence} covers any of the
 * classes in this predicate.
 */
public class IncludeIfCoversPredicate implements Predicate<ExecutableSequence> {

  /** the set of classes to be covered */
  private Set<Class<?>> coveredClasses;

  /**
   * Creates a predicate to test whether a sequence covers any of the given classes.
   *
   * @param coveredClasses the set of classes to be covered
   */
  public IncludeIfCoversPredicate(Set<Class<?>> coveredClasses) {
    this.coveredClasses = coveredClasses;
  }

  /**
   * {@inheritDoc}
   *
   * @return true if any of the classes in this predicate are covered by the sequence
   */
  @Override
  public boolean test(ExecutableSequence t) {
    for (Class<?> c : coveredClasses) {
      if (t.coversClass(c)) {
        return true;
      }
    }
    return false;
  }
}
