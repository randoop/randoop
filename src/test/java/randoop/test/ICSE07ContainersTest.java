package randoop.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static randoop.reflection.AccessibilityPredicate.IS_PUBLIC;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import randoop.generation.ComponentManager;
import randoop.generation.ForwardGenerator;
import randoop.generation.IStopper;
import randoop.generation.SeedSequences;
import randoop.generation.TestUtils;
import randoop.main.GenInputsAbstract;
import randoop.main.OptionsCache;
import randoop.operation.TypedOperation;
import randoop.reflection.AccessibilityPredicate;
import randoop.reflection.DefaultReflectionPredicate;
import randoop.reflection.OmitMethodsPredicate;
import randoop.reflection.OperationExtractor;
import randoop.test.issta2006.BinTree;
import randoop.test.issta2006.BinomialHeap;
import randoop.test.issta2006.FibHeap;
import randoop.test.issta2006.TreeMap;
import randoop.types.ClassOrInterfaceType;
import randoop.util.ReflectionExecutor;

/**
 * This test ensures that Randoop achieves a certain level of coverage across 4 data structures. The
 * coverage level that we check of is the one published in the ICSE 2007 paper "Feedback-directed
 * Random Test Generation" (Section 3.1).
 *
 * <p>For each data structure, we expect Randoop to achieve the published coverage in no longer than
 * 2 minutes.
 *
 * <p>Note that this test does not constitute the experiment published in the paper; it only checks
 * that the achievable coverage number can be in fact achieved by Randoop.
 *
 * <p>IMPORTANT: this test DOES NOT work if GenInputsAbstract.repeat_heuristic is disabled. If the
 * heuristic in {@link randoop.generation.ForwardGenerator ForwardGenerator} is not used, the branch
 * count targets are not met.
 */
public class ICSE07ContainersTest {

  private static OptionsCache optionsCache;

  @BeforeClass
  public static void setup() {
    optionsCache = new OptionsCache();
    optionsCache.saveState();
    GenInputsAbstract.maxsize = 10000; // Integer.MAX_VALUE;
    GenInputsAbstract.repeat_heuristic = true;
    GenInputsAbstract.debug_checks = false;
  }

  @AfterClass
  public static void restore() {
    optionsCache.restoreState();
  }

  private void runRandoop(
      String name,
      List<Class<?>> classList,
      List<Pattern> omitMethodPatterns,
      IStopper stopper,
      Set<String> excludeNames) {

    System.out.println("ICSE 2006 container: " + name);
    System.out.println("GenInputsAbstract.clear=" + GenInputsAbstract.clear);
    System.out.println("GenInputsAbstract.repeat_heuristic=" + GenInputsAbstract.repeat_heuristic);
    System.out.println("GenInputsAbstract.maxsize=" + GenInputsAbstract.maxsize);
    System.out.println("GenInputsAbstract.alias_ratio=" + GenInputsAbstract.alias_ratio);
    System.out.println("GenInputsAbstract.forbid_null=" + GenInputsAbstract.forbid_null);
    System.out.println("GenInputsAbstract.null_ratio=" + GenInputsAbstract.null_ratio);
    System.out.println("GenInputsAbstract.input_selection=" + GenInputsAbstract.input_selection);

    AccessibilityPredicate accessibility = IS_PUBLIC;
    Set<ClassOrInterfaceType> classesUnderTest = new HashSet<>();
    for (Class<?> c : classList) {
      ClassOrInterfaceType classType = ClassOrInterfaceType.forClass(c);
      classesUnderTest.add(classType);
    }
    final List<TypedOperation> model =
        OperationExtractor.operations(
            OperationExtractor.classListToTypeList(classList),
            new DefaultReflectionPredicate(excludeNames),
            new OmitMethodsPredicate(omitMethodPatterns),
            accessibility);
    assertFalse(model.isEmpty());
    System.out.println("Number of operations: " + model.size());

    ComponentManager componentMgr = new ComponentManager(SeedSequences.defaultSeeds());
    assertEquals(SeedSequences.defaultSeeds().size(), componentMgr.numGeneratedSequences());
    ForwardGenerator explorer =
        new ForwardGenerator(
            model,
            new LinkedHashSet<TypedOperation>(),
            new GenInputsAbstract.Limits(
                120 /* 2 minutes */, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE),
            componentMgr,
            stopper,
            null,
            classesUnderTest);
    explorer.setTestCheckGenerator(new DummyCheckGenerator());
    TestUtils.setAllLogs(explorer);
    explorer.createAndClassifySequences();
  }

  @Test
  public void testFibHeap() throws IOException {
    randoop.util.Randomness.setSeed(0);
    ReflectionExecutor.resetStatistics();

    final int goalBranches = 96;
    GenInputsAbstract.null_ratio = 0.05;
    List<Class<?>> classList = Collections.singletonList(FibHeap.class);
    FibHeap.rand.setSeed(0);
    randoop.util.Randomness.setSeed(0);
    IStopper stopper =
        new IStopper() {
          @Override
          public boolean shouldStop() {
            return FibHeap.branchFingerprints.size() >= goalBranches;
          }
        };
    Set<String> excludeNames = new TreeSet<>();
    for (Class<?> c : classList) {
      for (Field f : c.getFields()) {
        excludeNames.add(f.getDeclaringClass().getName() + "." + f.getName());
      }
    }
    List<Pattern> omitPatterns =
        Collections.singletonList(
            Pattern.compile(
                "decreaseKey|delete\\(randoop.test.issta2006.Node\\)|empty\\(\\)|insert\\(randoop.test.issta2006.Node\\)|min\\(\\)|size\\(\\)|union"));
    runRandoop("FibHeap", classList, omitPatterns, stopper, excludeNames);
    assertTrue(goalBranches <= FibHeap.branchFingerprints.size());
  }

  @Test
  public void testBinTree() {
    randoop.util.Randomness.setSeed(0);
    ReflectionExecutor.resetStatistics();

    final int goalBranches = 54;
    GenInputsAbstract.null_ratio = 0.5;
    List<Class<?>> classList = Collections.singletonList(BinTree.class);
    randoop.util.Randomness.setSeed(0);
    IStopper stopper =
        new IStopper() {
          @Override
          public boolean shouldStop() {
            return BinTree.branchFingerprints.size() >= goalBranches;
          }
        };
    Set<String> excludeNames = new TreeSet<>();
    for (Class<?> c : classList) {
      for (Field f : c.getFields()) {
        excludeNames.add(f.getDeclaringClass().getName() + "." + f.getName());
      }
    }
    List<Pattern> omitPatterns =
        Collections.singletonList(Pattern.compile("find\\(int\\)|gen_native"));
    runRandoop("BinTree", classList, omitPatterns, stopper, excludeNames);
    assertTrue(goalBranches <= BinTree.branchFingerprints.size());
  }

  @Test
  public void testTreeMap() {
    randoop.util.Randomness.setSeed(0);
    ReflectionExecutor.resetStatistics();

    final int goalBranches = 106;
    GenInputsAbstract.null_ratio = 0.05;
    List<Class<?>> classList = Collections.singletonList(TreeMap.class);
    randoop.util.Randomness.setSeed(0);
    IStopper stopper =
        new IStopper() {
          @Override
          public boolean shouldStop() {
            return TreeMap.branchFingerprints.size() >= goalBranches;
          }
        };
    Set<String> excludeNames = new TreeSet<>();
    for (Class<?> c : classList) {
      for (Field f : c.getFields()) {
        excludeNames.add(f.getDeclaringClass().getName() + "." + f.getName());
      }
    }
    List<Pattern> omitPatterns =
        Collections.singletonList(
            Pattern.compile(
                "toString\\(\\)|size\\(\\)|containsKey\\(int\\)|print\\(\\)|concreteString\\(int\\)"));
    runRandoop("TreeMap", classList, omitPatterns, stopper, excludeNames);
    assertTrue(goalBranches <= TreeMap.branchFingerprints.size());
  }

  @Test
  public void testBinomialHeap() {
    randoop.util.Randomness.setSeed(0);
    ReflectionExecutor.resetStatistics();

    final int goalBranches = 101;
    GenInputsAbstract.null_ratio = 0.05;
    List<Class<?>> classList = Collections.singletonList(BinomialHeap.class);
    randoop.util.Randomness.setSeed(0);
    IStopper stopper =
        new IStopper() {
          @Override
          public boolean shouldStop() {
            return BinomialHeap.branchFingerprints.size() >= goalBranches;
          }
        };
    Set<String> excludeNames = new TreeSet<>();
    for (Class<?> c : classList) {
      for (Field f : c.getFields()) {
        excludeNames.add(f.getDeclaringClass().getName() + "." + f.getName());
      }
    }
    List<Pattern> omitPatterns = Collections.singletonList(Pattern.compile("findMinimum\\(\\)"));
    runRandoop("BinomialHeap", classList, omitPatterns, stopper, excludeNames);
    assertTrue(goalBranches <= randoop.test.issta2006.BinomialHeap.branchFingerprints.size());
  }
}
