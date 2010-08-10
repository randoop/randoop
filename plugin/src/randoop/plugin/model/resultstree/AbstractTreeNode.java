package randoop.plugin.model.resultstree;

/**
 * A node in the Randoop results tree, with a link to its predecesor
 * in the tree.
 */
public abstract class AbstractTreeNode implements IRandoopTreeElement {

  public IRandoopTreeElement owner;

  @Override
  public abstract IRandoopTreeElement[] getChildren();
  
  @Override
  public IRandoopTreeElement getParent() {
    return owner;
  }

}
