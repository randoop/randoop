package randoop;


/** A visitor that does nothing and adds no checks. */
public final class DummyVisitor implements ExecutionVisitor {

  @Override
  public void initialize(ExecutableSequence executableSequence) {
    // do nothing.
  }
  
  public void visitBefore(ExecutableSequence sequence, int i) {
    // do nothing.
  }

  public void visitAfter(ExecutableSequence sequence, int i) {
    // do nothing.
  }

}
