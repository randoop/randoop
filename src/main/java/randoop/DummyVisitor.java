package randoop;

import randoop.sequence.ExecutableSequence;

/** A visitor that does nothing and adds no checks. */
public final class DummyVisitor implements ExecutionVisitor {

  @Override
  public void initialize(ExecutableSequence eseq) {
    // do nothing.
  }

  @Override
  public void visitBeforeStatement(ExecutableSequence eseq, int i) {
    // do nothing
  }

  @Override
  public void visitAfterStatement(ExecutableSequence eseq, int i) {
    // do nothing
  }

  @Override
  public void visitAfterSequence(ExecutableSequence eseq) {
    // do nothing
  }
}
