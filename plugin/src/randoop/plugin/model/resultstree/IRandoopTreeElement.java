package randoop.plugin.model.resultstree;

/**
 * Interface implemented by all the nodes in Randoop's results tree.
 */
public interface IRandoopTreeElement {

  IRandoopTreeElement[] getChildren();
  IRandoopTreeElement getParent();
  
}
