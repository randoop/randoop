package randoop.plugin.model.resultstree;

import java.util.LinkedHashMap;
import java.util.Map;

import randoop.runtime.ErrorRevealed;

/**
 * A node in the Randoop results tree that is the root node for all failures
 * reported by Randoop.
 */
public class Failures extends AbstractTreeNode {
  
  // Invariant: for all s in domain(failingClasses) : failingClasses[s].className equals s
  private final Map<String,FailureKind> failingClasses;

  public Failures() {
    this.failingClasses = new LinkedHashMap<String, FailureKind>();
  }
  
  @Override
  public IRandoopTreeElement[] getChildren() {
    return failingClasses.values().toArray(new IRandoopTreeElement[0]);
  }

  public void add(ErrorRevealed err) {
      FailureKind fc = failingClasses.get(err.description);
      if (fc == null) {
        fc = new FailureKind(err.description);
        failingClasses.put(err.description, fc);
      }
      fc.add(err);
  }

  public int size() {
    return failingClasses.size();
  }
}
