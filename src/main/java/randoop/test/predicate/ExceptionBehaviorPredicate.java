package randoop.test.predicate;

import randoop.ExceptionalExecution;
import randoop.main.ExceptionBehaviorClassifier;
import randoop.main.GenInputsAbstract.BehaviorType;
import randoop.sequence.ExecutableSequence;
import randoop.test.TestCheckGenerator;

/**
 * An exception predicate is used to check whether an exception corresponds to a
 * particular behavior. Used in {@link TestCheckGenerator} implementations
 * 
 * @see randoop.main.ExceptionBehaviorClassifier
 */
public class ExceptionBehaviorPredicate implements ExceptionPredicate {

  private BehaviorType behavior;

  /**
   * Creates a predicate that checks for the given behavior.
   * 
   * @param behavior
   *          the behavior to check for
   */
  public ExceptionBehaviorPredicate(BehaviorType behavior) {
    this.behavior = behavior;
  }

  /**
   * Test whether the {@code ExceptionalExecution} for the given
   * {@code ExecutableSequence} corresponds to the behavior set for this object.
   * 
   * @param exec
   *          the exceptional execution
   * @param s
   *          the sequence in which exception occurred
   * @return true if exception is classified with behavior, and false otherwise
   */
  @Override
  public boolean test(ExceptionalExecution exec, ExecutableSequence s) {
    Throwable exception = exec.getException();
    return ExceptionBehaviorClassifier.classify(exception, s) == behavior;
  }

}
