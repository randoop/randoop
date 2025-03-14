package randoop.test;

/**
 * An {@code InvalidExceptionCheck} represents the occurrence of an exception tagged as an invalid
 * behavior during {@code Check} generation.
 */
public class InvalidExceptionCheck extends ExceptionCheck {

  public InvalidExceptionCheck(Throwable exception, int statementIndex, String catchClassName) {
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
}
