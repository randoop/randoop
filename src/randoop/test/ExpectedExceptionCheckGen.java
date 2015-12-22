package randoop.test;

import randoop.EmptyExceptionCheck;
import randoop.ExceptionCheck;
import randoop.ExceptionalExecution;
import randoop.ExpectedExceptionCheck;
import randoop.reflection.VisibilityPredicate;
import randoop.sequence.ExecutableSequence;
import randoop.test.predicate.ExceptionPredicate;

/**
 * A check generator that based on an {@code ExceptionPredicate} when given an 
 * exception, generates an {@code ExpectedExceptionCheck} object when the 
 * predicate is satisfied, and an {@code EmptyExceptionCheck} otherwise.  
 * Resulting tests only enforce expected matching exceptions.
 * @see randoop.test.RegressionCaptureVisitor#visit(randoop.sequence.ExecutableSequence)
 */
public class ExpectedExceptionCheckGen {

  private ExceptionPredicate isExpected;
  private VisibilityPredicate visibility;

  /**
   * Create an object that generates expected exception checks for exceptions 
   * that satisfy the given predicate, and empty exception checks for others.
   * 
   * @param visibility  a predicate to determine visibility of exception classes
   */
  public ExpectedExceptionCheckGen(VisibilityPredicate visibility, ExceptionPredicate isExpected) {
    this.visibility = visibility;
    this.isExpected = isExpected;
  }
  
  /**
   * Constructs an {@code ExceptionCheck} for the given exception and statement
   * based on criteria of this generator.
   * 
   * @param exec  the exception outcome of executing the statement in a sequence
   * @param s  the sequence where exception occurred
   * @param statementIndex  the position of the statement in the sequence
   * @return an {@code ExpectedExceptionCheck} object for exception and statement
   * if exception is checked, and an {@code EmptyExceptionCheck} otherwise
   */
  public ExceptionCheck getExceptionCheck(ExceptionalExecution exec, ExecutableSequence s, int statementIndex) {
    Throwable e = exec.getException();
    String catchClassName = getCatchClassName(e.getClass());
    if (isExpected.test(exec,s)) {
      return new ExpectedExceptionCheck(e, statementIndex, catchClassName);
    } else {
      return new EmptyExceptionCheck(e, statementIndex, catchClassName);
    }
  }

  /** 
   * Returns the nearest visible superclass -- usually the argument itself. 
   * @param c  the class for which superclass is needed
   * @return the nearest public class that is the argument or a superclass
   */
  private Class<?> nearestVisibleSuperclass(Class<?> c) {
    while (! visibility.isVisible(c)) {
      c = c.getSuperclass();
    }
    return c;
  }

  /**
   * Returns the canonical name for the nearest visible class that will catch
   * an exception with the given class.
   * 
   * @param c  the exception class 
   * @return the nearest public visible, c or a superclass of c 
   */
  private String getCatchClassName(Class<? extends Throwable> c) {
    Class<?> catchClass = nearestVisibleSuperclass(c);
    return catchClass.getCanonicalName();
  }

}
