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
  void visitBefore(ExecutableSequence sequence, int i);

  /**
   * Invoked by ExecutableSequence.execute after the i-th statement executes.
   *
   * Precondition: statements 0..i have been executed.
   */
  void visitAfter(ExecutableSequence sequence, int i);

  /**
   * Called before execution of a sequence, to allow the visitor to
   * perform any initialization steps required before execution. 
   * @param executableSequence 
   */
  void initialize(ExecutableSequence executableSequence);

}
