package randoop.test;

import randoop.Globals;
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
    String prefix = "statement threw an invalid exception ";
    String suffix = " during test generation";
    b.append("// ")
        .append(prefix)
        .append(exception.getClass().getName())
        .append(suffix)
        .append(Globals.lineSep);
  }

  @Override
  protected void appendTryBehavior(StringBuilder b) {
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
