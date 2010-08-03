package randoop.plugin.model.resultstree;

import org.eclipse.jface.viewers.TreeViewer;

import randoop.runtime.ErrorRevealed;

/**
 * The root of Randoop's results tree.
 */
public class RunResultsTree implements IRandoopTreeElement {

  private Failures failures;

  public RunResultsTree() {
    this.failures = new Failures();
  }

  public void setFailures(Failures f) {
    this.failures = f;
    f.owner = this;
  }

  @Override
  public IRandoopTreeElement[] getChildren() {
    if (failures.getChildren().length == 0) {
      return new IRandoopTreeElement[0];
    } else {
      return new IRandoopTreeElement[] { failures };
    }
  }

  @Override
  public IRandoopTreeElement getParent() {
    return null;
  }

  public void add(ErrorRevealed err) {
    if (err == null) {
      throw new IllegalArgumentException("err is null"); //$NON-NLS-1$
    }
    failures.add(err);
  }

  public void reset() {
    failures = new Failures();
  }

}
