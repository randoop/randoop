package randoop.test;

import randoop.Globals;

/**
 * An {@code InvalidExceptionCheck} represents the occurrence of an exception tagged as an invalid
 * behavior during {@code Check} generation.
 */
public class InvalidExceptionCheck extends ExceptionCheck {

  public InvalidExceptionCheck(Throwable exception, int statementIndex, String catchClassName) {
    super(exception, statementIndex, catchClassName);
  }

  @Override
  protected void appendCatchBehavior(StringBuilder b, String catchClassName) {
    b.append("catch (").append(catchClassName).append(" e) {").append(Globals.lineSep);
    b.append("  // This is an expected exception.").append(Globals.lineSep);
    b.append(
        String.format(
            "// statement threw an invalid exception %s during test generation%n",
            exception.getClass().getName()));
  }

  @Override
  protected void appendTryBehavior(StringBuilder b) {
    // do nothing
  }
}
