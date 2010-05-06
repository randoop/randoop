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

  public MultiVisitor(List<ExecutionVisitor> visitors) {
    this.visitors.addAll(visitors);
  }

  public boolean visitAfter(ExecutableSequence sequence, int i) {
    for (ExecutionVisitor visitor : visitors) {
      boolean ret = visitor.visitAfter(sequence, i);
      if (ret == false)
        return false;
    }
    return true;
  }

  public void visitBefore(ExecutableSequence sequence, int i) {
    for (ExecutionVisitor visitor : visitors) {
      visitor.visitBefore(sequence, i);
    }
  }

}
