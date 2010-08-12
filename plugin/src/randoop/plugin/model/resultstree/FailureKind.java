package randoop.plugin.model.resultstree;

import java.util.LinkedHashMap;
import java.util.Map;

import randoop.runtime.ErrorRevealed;

/**
 * A node in the Randoop results tree representing a class
 * under test that resulted in failures during testing.
 */
public class FailureKind extends AbstractTreeNode {

  public final String className;
  private final Map<String,FailingMember> failures;
  
  public FailureKind(String className) {
    this.className = className;
    this.failures = new LinkedHashMap<String, FailingMember>();
  }

  @Override
  public IRandoopTreeElement[] getChildren() {
    return failures.values().toArray(new IRandoopTreeElement[0]);
  }
  
  public String getClassName() {
    return className;
  }

  public void add(ErrorRevealed err) {
    FailingMember failure = failures.get(err.description);
    if (failure == null) {
      for (String c : err.failingClassNames) {
        failure = new FailingMember(c, err.testCode, err.junitFile);
        failures.put(c, failure);
      }
    }
  }

}
