package randoop.plugin.model.resultstree;

/**
 * A node in the Randoop results tree, with a link to its predecesor
 * in the tree.
 */
public abstract class AbstractTreeNode implements IRandoopTreeElement {

  public IRandoopTreeElement owner;

  public abstract IRandoopTreeElement[] getChildren();
  
  public IRandoopTreeElement getParent() {
    return owner;
  }

}
