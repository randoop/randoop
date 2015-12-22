package randoop.test;

import randoop.Check;
import randoop.sequence.Execution;

/**
 * An {@code InvalidExceptionCheck} represents the occurrence of an exception
 * tagged as an invalid behavior during {@code Check} generation.
 */
public class InvalidExceptionCheck implements Check {

  private static final long serialVersionUID = -4919647365672571645L;
  private int statementIndex;
  private Throwable exception;

  public InvalidExceptionCheck(Throwable exception, int statementIndex) {
    this.exception = exception;
    this.statementIndex = statementIndex;
  }

  @Override
  public String toCodeStringPreStatement() {
    return "// statement throws an invalid exception " + exception.getClass().getName();
  }

  @Override
  public String toCodeStringPostStatement() {
    // 
    return "";
  }

  @Override
  public String getValue() {
    return "invalid exception " + exception.getClass().getName();
  }

  @Override
  public String getID() {
    return "Invalid(" + exception.getClass().getCanonicalName() + ")";
  }

  @Override
  public int getStatementIndex() {
    return statementIndex;
  }

  /**
   * {@inheritDoc}
   * An invalid check cannot be evaluated, so this throws an exception.
   * @throws IllegalArgumentException whenever called
   */
  @Override
  public boolean evaluate(Execution execution) {
    throw new IllegalArgumentException("Cannot evaluate an invalid check");
  }

}
