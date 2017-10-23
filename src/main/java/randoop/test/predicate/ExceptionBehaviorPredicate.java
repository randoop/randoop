package randoop.test.predicate;

import randoop.ExceptionalExecution;
import randoop.main.ExceptionBehaviorClassifier;
import randoop.main.GenInputsAbstract.BehaviorType;
import randoop.sequence.ExecutableSequence;
import randoop.test.TestCheckGenerator;

/**
 * An exception predicate is used to check whether an exception corresponds to a particular
 * behavior. Used in {@link TestCheckGenerator} implementations
 *
 * @see randoop.main.ExceptionBehaviorClassifier
 */
public class ExceptionBehaviorPredicate implements ExceptionPredicate {

  /** A predicate that checks for invalid behavior. */
  public static ExceptionBehaviorPredicate IS_INVALID =
      new ExceptionBehaviorPredicate(BehaviorType.INVALID);
  /** A predicate that checks for error behavior. */
  public static ExceptionBehaviorPredicate IS_ERROR =
      new ExceptionBehaviorPredicate(BehaviorType.ERROR);
  /** A predicate that checks for expected behavior. */
  public static ExceptionBehaviorPredicate IS_EXPECTED =
      new ExceptionBehaviorPredicate(BehaviorType.EXPECTED);

  private BehaviorType behavior;

  /**
   * Creates a predicate that checks for the given behavior.
   *
   * @param behavior the behavior to check for
   */
  private ExceptionBehaviorPredicate(BehaviorType behavior) {
    this.behavior = behavior;
  }

  /**
   * Test whether the {@code ExceptionalExecution} for the given {@code ExecutableSequence}
   * corresponds to the behavior set for this object.
   *
   * @param exec the exceptional execution
   * @param eseq the sequence in which exception occurred
   * @return true if exception is classified with behavior, and false otherwise
   */
  @Override
  public boolean test(ExceptionalExecution exec, ExecutableSequence eseq) {
    Throwable exception = exec.getException();
    return ExceptionBehaviorClassifier.classify(exception, eseq) == behavior;
  }
}
