package randoop.test;

import randoop.sequence.Execution;

/**
 * An {@code InvalidExceptionCheck} represents the occurrence of an exception
 * tagged as an invalid behavior during {@code Check} generation.
 */
public class InvalidExceptionCheck extends ExceptionCheck {

  public InvalidExceptionCheck(Throwable exception, int statementIndex, String catchClassName) {
    super(exception, statementIndex, catchClassName);
  }

  @Override
  protected void appendCatchBehavior(StringBuilder b, String exceptionClassName) {
    String prefix = "statement threw an invalid exception ";
    String suffix = " during test generation";
    b.append("// " + prefix + exception.getClass().getName() + suffix);
  }

  @Override
  protected void appendTryBehavior(StringBuilder b, String exceptionClassName) {
    // do nothing
  }

  @Override
  public String getValue() {
    return "invalid exception " + exception.getClass().getName();
  }

  @Override
  public String getID() {
    return "Invalid(" + exception.getClass().getCanonicalName() + ")";
  }

  /**
   * {@inheritDoc} An invalid check cannot be evaluated, so this throws an
   * exception.
   *
   * @throws IllegalArgumentException
   *           whenever called
   */
  @Override
  public boolean evaluate(Execution execution) {
    throw new IllegalArgumentException("Cannot evaluate an invalid check");
  }
}
