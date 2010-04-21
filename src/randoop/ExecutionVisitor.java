package randoop;


/**
 * A visitor that is invoked as the execution of a sequence unfolds.
 * Typically such a visitor adds decorations to the sequence based
 * on observations of the runtime behavior.
 *
 * IMPORTANT: Implementing classes should have a zero-argument
 * constructor.
 */
public interface ExecutionVisitor {

  /**
   * Invoked by ExecutableSequence.execute before
   * the i-th statement executes.
   *
   * Precondition: statements 0..i-1 have been executed.
   */
  public abstract void visitBefore(ExecutableSequence sequence, int i);

  /**
   * Invoked by ExecutableSequence.execute after the i-th statement executes.
   * The return value signals whether the execution of the sequence should be
   * aborted. If this method returns false, the remaining statements in the
   * sequence under execution will not be executed.
   *
   * Precondition: statements 0..i have been executed.
   */
  public abstract boolean visitAfter(ExecutableSequence sequence, int i);

}
