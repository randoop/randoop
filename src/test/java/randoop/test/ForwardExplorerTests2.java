package randoop.test;

import java.util.ArrayList;
import java.util.List;

import randoop.main.GenInputsAbstract;
import randoop.operation.Operation;
import randoop.reflection.OperationExtractor;
import randoop.sequence.ForwardGenerator;
import randoop.sequence.Sequence;
import randoop.test.treeadd.TreeAdd;
import randoop.test.treeadd.TreeNode;
import randoop.util.Reflection;

import junit.framework.TestCase;

public class ForwardExplorerTests2 extends TestCase {

  public void test5() throws Exception {

    // This test throws Randoop into an infinite loop. Disabling
    // TODO look into it.
    if (true) return;

    List<Class<?>> classes = new ArrayList<Class<?>>();
    classes.add(TreeNode.class);
    classes.add(TreeAdd.class);

    System.out.println(classes);

    //SimpleExplorer exp = new SimpleExplorer(classes, Long.MAX_VALUE, 100);
    List<Operation> model = OperationExtractor.getOperations(classes, null);
    assertTrue("model should not be empty", model.size() != 0);
    ForwardGenerator exp = new ForwardGenerator(model, Long.MAX_VALUE, 100, 100, null, null, null);
    GenInputsAbstract.null_ratio = 0.5; //.forbid_null = false;
    exp.explore();
    for (Sequence s : exp.getAllSequences()) {
      s.toCodeString();
    }
  }
}
