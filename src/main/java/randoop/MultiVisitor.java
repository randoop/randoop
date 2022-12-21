package randoop;

import java.util.ArrayList;
import java.util.List;
import randoop.sequence.ExecutableSequence;

/**
 * An execution visitor that chains a list of visitors in sequence. It can be used if the user wants
 * to use more than one visitor to visit a sequence during execution.
 *
 * <p>When the visitBefore method is called on this visitor, it calls visitMethod on each of the
 * sub-visitors in the order in which the sub-visitors were given when constructing this visitor.
 *
 * <p>When the visitAfter method is called on this visitor, it calls visitAfter on each of the
 * sub-visitors in turn, also in the order in which they were given when constructing the visitor.
 * If one of these calls returns false, this visitor immediately returns false without calling
 * visitAfter on the remaining visitors.
 */
public class MultiVisitor implements ExecutionVisitor {

  /** The list of visitors. */
  private final List<ExecutionVisitor> visitors = new ArrayList<>();

  /** Creates a new MultiVisitor. */
  public MultiVisitor() {}

  /**
   * Creates a new MultiVisitor.
   *
   * @param visitors the list of visitors
   */
  public MultiVisitor(List<ExecutionVisitor> visitors) {
    this.visitors.addAll(visitors);
  }

  /**
   * Returns a MultiVisitor if needed, otherwise a simpler visitor.
   *
   * @param visitors the visitors to compose
   * @return a visitor that has the effect of all the visitors in the argument
   */
  public static ExecutionVisitor createMultiVisitor(List<ExecutionVisitor> visitors) {
    switch (visitors.size()) {
      case 0:
        return new DummyVisitor();
      case 1:
        return visitors.get(0);
      default:
        return new MultiVisitor(visitors);
    }
  }

  /**
   * Calls the initialize method for each of the visitors, in the order in which the visitors were
   * given during construction of this MultiVisitor.
   */
  @Override
  public void initialize(ExecutableSequence eseq) {
    for (ExecutionVisitor visitor : visitors) {
      visitor.initialize(eseq);
    }
  }

  @Override
  public void visitAfterStatement(ExecutableSequence eseq, int i) {
    for (ExecutionVisitor visitor : visitors) {
      visitor.visitAfterStatement(eseq, i);
    }
  }

  @Override
  public void visitBeforeStatement(ExecutableSequence eseq, int i) {
    for (ExecutionVisitor visitor : visitors) {
      visitor.visitBeforeStatement(eseq, i);
    }
  }

  @Override
  public void visitAfterSequence(ExecutableSequence eseq) {
    for (ExecutionVisitor visitor : visitors) {
      visitor.visitAfterSequence(eseq);
    }
  }
}
