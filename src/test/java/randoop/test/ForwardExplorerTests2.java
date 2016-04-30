package randoop.test;

import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import randoop.contract.ObjectContract;
import randoop.generation.ComponentManager;
import randoop.generation.ForwardGenerator;
import randoop.generation.SeedSequences;
import randoop.main.GenInputsAbstract;
import randoop.main.GenTests;
import randoop.operation.ConcreteOperation;
import randoop.reflection.DefaultReflectionPredicate;
import randoop.reflection.ModelCollections;
import randoop.reflection.OperationExtractor;
import randoop.reflection.PublicVisibilityPredicate;
import randoop.reflection.ReflectionManager;
import randoop.reflection.TypedOperationManager;
import randoop.sequence.Sequence;
import randoop.test.treeadd.TreeAdd;
import randoop.test.treeadd.TreeNode;
import randoop.types.ConcreteType;
import randoop.util.MultiMap;

import static org.junit.Assert.assertTrue;


public class ForwardExplorerTests2  {

  @Test
  public void test5() throws Exception {

    // This test throws Randoop into an infinite loop. Disabling
    // TODO look into it.
    //if (true) return;

    List<Class<?>> classes = new ArrayList<>();
    classes.add(TreeNode.class);
    classes.add(TreeAdd.class);

    System.out.println(classes);

    //SimpleExplorer exp = new SimpleExplorer(classes, Long.MAX_VALUE, 100);
    List<ConcreteOperation> model = getConcreteOperations(classes);
    assertTrue("model should not be empty", !model.isEmpty());
    ComponentManager mgr = new ComponentManager(SeedSequences.defaultSeeds());
    ForwardGenerator exp = new ForwardGenerator(model, new LinkedHashSet<ConcreteOperation>(), Long.MAX_VALUE, 100, 100, mgr, null, null);
    exp.addTestCheckGenerator(createChecker(new LinkedHashSet<ObjectContract>()));
    GenInputsAbstract.null_ratio = 0.5; //.forbid_null = false;
    exp.explore();
    for (Sequence s : exp.getAllSequences()) {
      s.toCodeString();
    }
  }

  private static List<ConcreteOperation> getConcreteOperations(List<Class<?>> classes) {
    final List<ConcreteOperation> model = new ArrayList<>();
    TypedOperationManager operationManager = new TypedOperationManager(new ModelCollections() {
      @Override
      public void addConcreteOperation(ConcreteType declaringType, ConcreteOperation operation) {
        model.add(operation);
      }
    });
    ReflectionManager mgr = new ReflectionManager(new PublicVisibilityPredicate());
    mgr.add(new OperationExtractor(operationManager, new DefaultReflectionPredicate()));
    for (Class<?> c: classes) {
      mgr.apply(c);
    }
    return model;
  }

  private static TestCheckGenerator createChecker(Set<ObjectContract> contracts) {
    return (new GenTests()).createTestCheckGenerator(new PublicVisibilityPredicate(), contracts, new MultiMap<ConcreteType, ConcreteOperation>(), new LinkedHashSet<ConcreteOperation>());
  }
}
