package randoop;

import randoop.sequence.ExecutableSequence;

/** A visitor that does nothing and adds no checks. */
public final class DummyVisitor implements ExecutionVisitor {

  @Override
  public void initialize(ExecutableSequence executableSequence) {
    // do nothing.
  }

  @Override
  public void visitBeforeStatement(ExecutableSequence sequence, int i) {
    // do nothing
  }

  @Override
  public void visitAfterStatement(ExecutableSequence sequence, int i) {
    // do nothing
  }

  @Override
  public void visitAfterSequence(ExecutableSequence executableSequence) {
    // do nothing
  }

}
