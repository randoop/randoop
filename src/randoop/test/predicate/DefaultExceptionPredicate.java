package randoop.test;

/**
 * Implements default behaviors of the {@code ExceptionPredicate} interface.
 * Specifically, the {@code or} creation method.
 */
public abstract class DefaultExceptionPredicate implements ExceptionPredicate {

  @Override
  public ExceptionPredicate or(ExceptionPredicate p) {
    return new OrExceptionPredicate(this,p);
  }

}
