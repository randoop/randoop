package randoop.test;

import static randoop.main.GenInputsAbstract.BehaviorType.EXPECTED;

import randoop.ExceptionalExecution;
import randoop.main.ExceptionBehaviorClassifier;
import randoop.reflection.AccessibilityPredicate;
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

  /** A predicate to determine accessibility of exception classes. */
  private AccessibilityPredicate accessibility;

  /**
   * Create an object that generates expected exception checks for exceptions that satisfy the given
   * predicate, and empty exception checks for others.
   *
   * @param accessibility a predicate to determine accessibility of exception classes
   */
  public ExpectedExceptionCheckGen(AccessibilityPredicate accessibility) {
    this.accessibility = accessibility;
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

    String catchClassName = getCatchClassName(e.getClass(), accessibility);

    if (ExceptionBehaviorClassifier.classify(exec, eseq) == EXPECTED) {
      return new ExpectedExceptionCheck(e, statementIndex, catchClassName);
    } else {
      return new EmptyExceptionCheck(e, statementIndex, catchClassName);
    }
  }

  /**
   * Returns the nearest accessible superclass -- usually the argument itself.
   *
   * @param c the class for which superclass is needed
   * @param accessibility only superclasess satisfying this predicate may be returned
   * @return the nearest public class that is the argument or a superclass
   */
  private static Class<?> nearestAccessibleSuperclass(
      Class<?> c, AccessibilityPredicate accessibility) {
    while (!accessibility.isAccessible(c)) {
      c = c.getSuperclass();
    }
    return c;
  }

  /**
   * Returns the canonical name for the nearest accessible class that will catch an exception with
   * the given class.
   *
   * @param c the exception class
   * @param accessibility only superclasess satisfying this predicate may be returned
   * @return the nearest public accessible, c or a superclass of c
   */
  public static String getCatchClassName(
      Class<? extends Throwable> c, AccessibilityPredicate accessibility) {
    Class<?> catchClass = nearestAccessibleSuperclass(c, accessibility);
    return catchClass.getCanonicalName();
  }

  /**
   * Returns the canonical name for the nearest public class that will catch an exception with the
   * given class.
   *
   * @param c the exception class
   * @return the nearest public accessible, c or a superclass of c
   */
  public static String getCatchClassName(Class<? extends Throwable> c) {
    Class<?> catchClass = nearestAccessibleSuperclass(c, AccessibilityPredicate.IS_PUBLIC);
    return catchClass.getCanonicalName();
  }
}
