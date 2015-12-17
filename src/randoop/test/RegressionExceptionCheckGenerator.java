package randoop.test;

import randoop.ExceptionCheck;
import randoop.ExceptionalExecution;

/**
 * Interface for objects that generate different types of {@code ExceptionCheck}
 * objects to use when determining test checks from the execution of a sequence.
 * @see randoop.RegressionCaptureVisitor#visit(randoop.sequence.ExecutableSequence)
 */
public interface RegressionExceptionCheckGenerator {
  
  /**
   * Constructs an {@code ExceptionCheck} for the given exception and statement
   * based on criteria of this generator.
   * 
   * @param e  the exception outcome of executing the statement in a sequence
   * @param statementIndex  the position of the statement in the sequence
   * @return an {@code ExceptionCheck} object for the statement
   */
  ExceptionCheck getExceptionCheck(ExceptionalExecution e, int statementIndex);
}
