package randoop.test;

import static randoop.main.GenInputsAbstract.BehaviorType.EXPECTED;

import randoop.ExceptionalExecution;
import randoop.main.ExceptionBehaviorClassifier;
import randoop.reflection.VisibilityPredicate;
import randoop.sequence.ExecutableSequence;

/**
 * A check generator that when given an exception, generates either an {@code
 * ExpectedExceptionCheck} object or an {@code EmptyExceptionCheck}. Resulting tests only enforce
 * expected matching exceptions.
 *
 * @see
 *     randoop.test.RegressionCaptureGenerator#generateTestChecks(randoop.sequence.ExecutableSequence)
 */
public class ExpectedExceptionCheckGen {

  /** a predicate to determine visibility of exception classes */
  private VisibilityPredicate visibility;

  /**
   * Create an object that generates expected exception checks for exceptions that satisfy the given
   * predicate, and empty exception checks for others.
   *
   * @param visibility a predicate to determine visibility of exception classes
   */
  public ExpectedExceptionCheckGen(VisibilityPredicate visibility) {
    this.visibility = visibility;
  }

  /**
   * Constructs an {@code ExceptionCheck} for the given exception and statement based on criteria of
   * this generator.
   *
   * @param exec the exception outcome of executing the statement in a sequence
   * @param eseq the sequence where exception occurred
   * @param statementIndex the position of the statement in the sequence
   * @return an {@code ExpectedExceptionCheck} object for exception and statement if exception is
   *     checked, and an {@code EmptyExceptionCheck} otherwise
   */
  ExceptionCheck getExceptionCheck(
      ExceptionalExecution exec, ExecutableSequence eseq, int statementIndex) {
    Throwable e = exec.getException();

    String catchClassName = getCatchClassName(e.getClass(), visibility);

    if (ExceptionBehaviorClassifier.classify(exec, eseq) == EXPECTED) {
      return new ExpectedExceptionCheck(e, statementIndex, catchClassName);
    } else {
      return new EmptyExceptionCheck(e, statementIndex, catchClassName);
    }
  }

  /**
   * Returns the nearest visible superclass -- usually the argument itself.
   *
   * @param c the class for which superclass is needed
   * @param visibility only superclasess satisfying this predicate may be returned
   * @return the nearest public class that is the argument or a superclass
   */
  private static Class<?> nearestVisibleSuperclass(Class<?> c, VisibilityPredicate visibility) {
    while (!visibility.isVisible(c)) {
      c = c.getSuperclass();
    }
    return c;
  }

  /**
   * Returns the canonical name for the nearest visible class that will catch an exception with the
   * given class.
   *
   * @param c the exception class
   * @param visibility only superclasess satisfying this predicate may be returned
   * @return the nearest public visible, c or a superclass of c
   */
  public static String getCatchClassName(
      Class<? extends Throwable> c, VisibilityPredicate visibility) {
    Class<?> catchClass = nearestVisibleSuperclass(c, visibility);
    return catchClass.getCanonicalName();
  }

  /**
   * Returns the canonical name for the nearest public class that will catch an exception with the
   * given class.
   *
   * @param c the exception class
   * @return the nearest public visible, c or a superclass of c
   */
  public static String getCatchClassName(Class<? extends Throwable> c) {
    Class<?> catchClass = nearestVisibleSuperclass(c, VisibilityPredicate.IS_PUBLIC);
    return catchClass.getCanonicalName();
  }
}
