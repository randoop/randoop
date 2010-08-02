package randoop.plugin.model.resultstree;

import java.io.File;


/**
 * A node in the Randoop results tree representing a specific kind of failure
 * observed for a given class, for example, "checkRep violation" or
 * "equals/hashCode violation" or "null-pointer exception thrown".
 */
public class FailingMember extends AbstractTreeNode {
  
  private static final IRandoopTreeElement[] EMPTY_ARRAY = new IRandoopTreeElement[0];
  
  public final String description;
  public final UnitTest witnessTest;

  public FailingMember(String description, String testCode, File junitFile) {
    this.description = description;
    this.witnessTest = new UnitTest(testCode, junitFile);
  }

  @Override
  public IRandoopTreeElement[] getChildren() {
    return EMPTY_ARRAY;
  }

}
