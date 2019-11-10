package randoop;

import randoop.sequence.ExecutableSequence;

/**
 * A visitor that is invoked as the execution of a sequence unfolds. Typically such a visitor adds
 * decorations to the sequence based on checks of the runtime behavior.
 *
 * <p>IMPORTANT: Implementing classes should have a default constructor.
 */
public interface ExecutionVisitor {

  /**
   * Invoked by ExecutableSequence.execute before the i-th statement executes.
   *
   * <p>Precondition: statements 0..i-1 have been executed.
   *
   * @param eseq the code sequence to be visited
   * @param i the position of statement to visit
   */
  void visitBeforeStatement(ExecutableSequence eseq, int i);

  /**
   * Invoked by ExecutableSequence.execute after the i-th statement executes.
   *
   * <p>Precondition: statements 0..i have been executed.
   *
   * @param eseq the code sequence to be visited
   * @param i the position of statement to visit
   */
  void visitAfterStatement(ExecutableSequence eseq, int i);

  /**
   * Called before execution of a sequence, to allow the visitor to perform any initialization steps
   * required before execution.
   *
   * @param eseq the code sequence to be visited
   */
  void initialize(ExecutableSequence eseq);

  /**
   * Called after execution of a sequence.
   *
   * @param eseq the visited code sequence
   */
  void visitAfterSequence(ExecutableSequence eseq);
}
