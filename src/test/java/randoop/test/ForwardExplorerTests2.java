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
import randoop.operation.TypedOperation;
import randoop.reflection.DefaultReflectionPredicate;
import randoop.reflection.ModelCollections;
import randoop.reflection.OperationExtractor;
import randoop.reflection.PublicVisibilityPredicate;
import randoop.reflection.ReflectionManager;
import randoop.reflection.TypedOperationManager;
import randoop.sequence.Sequence;
import randoop.sequence.SequenceExceptionError;
import randoop.test.treeadd.TreeAdd;
import randoop.test.treeadd.TreeNode;
import randoop.types.ClassOrInterfaceType;
import randoop.types.GeneralType;
import randoop.util.MultiMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


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
    List<TypedOperation> model = getConcreteOperations(classes);
    assertTrue("model should not be empty", model.size() != 0);
    ComponentManager mgr = new ComponentManager(SeedSequences.defaultSeeds());
    ForwardGenerator exp = new ForwardGenerator(model, new LinkedHashSet<TypedOperation>(), Long.MAX_VALUE, 100, 100, mgr, null, null);
    exp.addTestCheckGenerator(createChecker(new LinkedHashSet<ObjectContract>()));
    GenInputsAbstract.null_ratio = 0.05; //.forbid_null = false;
    try {
      exp.explore();
      fail("expected timeout exception");
    } catch (SequenceExceptionError e) {
      assertEquals("should be timeout", e.getMessage(), "Exception thrown before end of sequence");
    }
    for (Sequence s : exp.getAllSequences()) {
      s.toCodeString();
    }
  }

  private static List<TypedOperation> getConcreteOperations(List<Class<?>> classes) {
    final List<TypedOperation> model = new ArrayList<>();
    TypedOperationManager operationManager = new TypedOperationManager(new ModelCollections() {
      @Override
      public void addConcreteOperation(ClassOrInterfaceType declaringType, TypedOperation operation) {
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
    return (new GenTests()).createTestCheckGenerator(new PublicVisibilityPredicate(), contracts, new MultiMap<GeneralType, TypedOperation>(), new LinkedHashSet<TypedOperation>());
  }
}
