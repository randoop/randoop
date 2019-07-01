package randoop.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static randoop.reflection.VisibilityPredicate.IS_PUBLIC;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import randoop.generation.ComponentManager;
import randoop.generation.ForwardGenerator;
import randoop.generation.SeedSequences;
import randoop.main.GenInputsAbstract;
import randoop.main.GenTests;
import randoop.main.OptionsCache;
import randoop.operation.TypedOperation;
import randoop.reflection.DefaultReflectionPredicate;
import randoop.reflection.OmitMethodsPredicate;
import randoop.reflection.OperationExtractor;
import randoop.reflection.ReflectionManager;
import randoop.reflection.VisibilityPredicate;
import randoop.sequence.Sequence;
import randoop.sequence.SequenceExceptionError;
import randoop.test.treeadd.TreeAdd;
import randoop.test.treeadd.TreeNode;
import randoop.types.ClassOrInterfaceType;
import randoop.util.MultiMap;
import randoop.util.ReflectionExecutor;

/**
 * This test is disabled in build.gradle.
 *
 * <p>It has a sporadic Java heap space exception caught by the Throwable clause of the try block
 * for the call to exp.createAndClassifySequences() in test5().
 *
 * <p>Mostly occurs when testing on Travis in Oracle JDK 7 or Open JDK 7 configurations, but I have
 * also gotten it during runs on Oracle JDK 8 on my mac.
 *
 * <p>Tried setting maxHeapSize in the test task configuration in the build script, but only took it
 * down to 200. Plus, it is not clear what the heap size is on Travis, so need to check that.
 */
public class ForwardExplorerTests2 {

  private static OptionsCache optionsCache;

  @BeforeClass
  public static void setup() {
    optionsCache = new OptionsCache();
    optionsCache.saveState();
  }

  @AfterClass
  public static void restore() {
    optionsCache.restoreState();
  }

  /**
   * The input scenario for this test results in the generation of a sequence with repeated calls to
   * a non-terminating method. If {@code --usethreads} is set, the generator is not able to
   * interrupt the executor, and will never terminate. Otherwise, a timeout exception will be thrown
   * and the executor will throw an exception, which since it is not the last statement is
   * considered "flaky".
   */
  @Test
  public void test5() throws Exception {
    // means that timeout results in a flaky sequence exception
    GenInputsAbstract.repeat_heuristic = true;

    assertTrue(
        ReflectionExecutor.usethreads); // This test does not terminate if threads are not used.

    List<Class<?>> classes = new ArrayList<>();
    classes.add(TreeNode.class);
    classes.add(TreeAdd.class);

    System.out.println(classes);

    // SimpleExplorer exp = new SimpleExplorer(classes, Long.MAX_VALUE, 100);
    List<TypedOperation> model = getConcreteOperations(classes);
    assertTrue("model should not be empty", model.size() != 0);
    ComponentManager mgr = new ComponentManager(SeedSequences.defaultSeeds());
    ForwardGenerator exp =
        new ForwardGenerator(
            model,
            new LinkedHashSet<TypedOperation>(),
            new GenInputsAbstract.Limits(0, 100, 100, 100),
            mgr,
            null,
            null);
    exp.setTestCheckGenerator(createChecker(new ContractSet()));

    // get a SequenceExceptionError when repeat_heuristic=true
    try {
      exp.createAndClassifySequences();
      // fail("expected timeout exception");
    } catch (SequenceExceptionError e) {
      assertEquals("should be timeout", e.getMessage(), "Exception thrown before end of sequence");
    } catch (Throwable t) {
      fail("got an unexpected exception: " + t.getMessage());
    }
    for (Sequence s : exp.getAllSequences()) {
      s.toCodeString();
    }
  }

  private static List<TypedOperation> getConcreteOperations(List<Class<?>> classes) {
    final List<TypedOperation> model = new ArrayList<>();
    VisibilityPredicate visibility = IS_PUBLIC;
    ReflectionManager mgr = new ReflectionManager(visibility);
    for (Class<?> c : classes) {
      ClassOrInterfaceType classType = ClassOrInterfaceType.forClass(c);
      final OperationExtractor extractor =
          new OperationExtractor(classType, new DefaultReflectionPredicate(), visibility);
      mgr.apply(extractor, c);
      model.addAll(extractor.getOperations());
    }
    return model;
  }

  private static TestCheckGenerator createChecker(ContractSet contracts) {
    return GenTests.createTestCheckGenerator(
        IS_PUBLIC, contracts, new MultiMap<>(), new OmitMethodsPredicate(null));
  }
}
