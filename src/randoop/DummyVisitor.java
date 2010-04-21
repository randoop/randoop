package randoop;


/** A visitor that does nothing and adds no decorations. */
public final class DummyVisitor implements ExecutionVisitor {

  public void visitBefore(ExecutableSequence sequence, int i) {
    // do nothing.
  }

  public boolean visitAfter(ExecutableSequence sequence, int i) {
    // do nothing.
    return true;
  }

}
