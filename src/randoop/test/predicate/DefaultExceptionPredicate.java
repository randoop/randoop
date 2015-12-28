package randoop.test.predicate;

/**
 * Implements default behaviors of the {@code ExceptionPredicate} interface.
 * Specifically, the {@code or} creation method.
 */
public abstract class DefaultExceptionPredicate implements ExceptionPredicate {

  @Override
  public ExceptionPredicate or(ExceptionPredicate p) {
    return new OrExceptionPredicate(this,p);
  }
  
  @Override
  public ExceptionPredicate and(ExceptionPredicate p) {
    return new AndExceptionPredicate(this,p);
  }

  @Override
  public ExceptionPredicate not() {
    return new NotExceptionPredicate(this);
  }
  
}
