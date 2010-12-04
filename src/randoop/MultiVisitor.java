package randoop;

import java.util.ArrayList;
import java.util.List;

/**
 * An execution visitor that chains a list of visitors in sequence. It can be
 * used if the user wants to use more than one visitor to visit a sequence
 * during execution.
 * 
 * When the visitBefore method is called on this visitor, it calls visitMethod
 * on each of the sub-visitors in the order in which the sub-visitors were given
 * when constructing this visitor.
 * 
 * When the visitAfter method is called on this visitor, it calls visitAfter on
 * each of the sub-visitors in turn, also in the order in which they were given
 * when constructing the visitor. If one of these calls returns false, this
 * visitor immediately returns false without calling visitAfter on the remaining
 * visitors.
 */
public class MultiVisitor implements ExecutionVisitor {
  
  // The list of visitors. 
  public final List<ExecutionVisitor> visitors =
    new ArrayList<ExecutionVisitor>();

  public MultiVisitor() { }

  /**
   * Calls the initialize method for each of the visitors, in the
   * order in which the visitors were given during construction of
   * this MultiVisitor.
   */
  @Override
  public void initialize(ExecutableSequence executableSequence) {
    for (ExecutionVisitor visitor : visitors) {
      visitor.initialize(executableSequence);
    }    
  }
  
  public MultiVisitor(List<ExecutionVisitor> visitors) {
    this.visitors.addAll(visitors);
  }

  public void visitAfter(ExecutableSequence sequence, int i) {
    for (ExecutionVisitor visitor : visitors) {
      visitor.visitAfter(sequence, i);
    }
  }

  public void visitBefore(ExecutableSequence sequence, int i) {
    for (ExecutionVisitor visitor : visitors) {
      visitor.visitBefore(sequence, i);
    }
  }


}
