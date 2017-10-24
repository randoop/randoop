package randoop.test;

import randoop.sequence.Execution;

/**
 * An {@code InvalidExceptionCheck} represents the occurrence of an exception tagged as an invalid
 * behavior during {@code Check} generation.
 */
class InvalidExceptionCheck extends ExceptionCheck {

  InvalidExceptionCheck(Throwable exception, int statementIndex, String catchClassName) {
    super(exception, statementIndex, catchClassName);
  }

  @Override
  protected void appendCatchBehavior(StringBuilder b) {
    b.append(
        String.format(
            "// statement threw an invalid exception %s during test generation%n",
            exception.getClass().getName()));
  }

  @Override
  protected void appendTryBehavior(StringBuilder b) {
    // do nothing
  }

  /**
   * {@inheritDoc}
   *
   * <p>An invalid check cannot be evaluated, so this throws an exception.
   *
   * @throws IllegalArgumentException whenever called
   */
  @Override
  public boolean evaluate(Execution execution) {
    throw new IllegalArgumentException("Cannot evaluate an invalid check");
  }
}
