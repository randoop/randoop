package randoop;


/**
 * A visitor that is invoked as the execution of a sequence unfolds.
 * Typically such a visitor adds decorations to the sequence based
 * on checks of the runtime behavior.
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
  abstract void visitBefore(ExecutableSequence sequence, int i);

  /**
   * Invoked by ExecutableSequence.execute after the i-th statement executes.
   * The return value signals whether the execution of the sequence should be
   * aborted. If this method returns false, the remaining statements in the
   * sequence under execution will not be executed.
   *
   * Precondition: statements 0..i have been executed.
   */
  abstract boolean visitAfter(ExecutableSequence sequence, int i);

  /**
   * Called before execution of a sequence, to allow the visitor to
   * perform any initialization steps required before execution. 
   * @param executableSequence 
   */
  void initialize(ExecutableSequence executableSequence);

}
