package randoop.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import junit.framework.TestCase;
import randoop.ComponentManager;
import randoop.EverythingIsDifferentMatcher;
import randoop.ForwardGenerator;
import randoop.IStopper;
import randoop.ObjectCache;
import randoop.SeedSequences;
import randoop.StatementKind;
import randoop.main.GenInputsAbstract;
import randoop.test.issta2006.BinTree;
import randoop.test.issta2006.BinomialHeap;
import randoop.test.issta2006.FibHeap;
import randoop.test.issta2006.TreeMap;
import randoop.util.DefaultReflectionFilter;
import randoop.util.Reflection;
import randoop.util.ReflectionExecutor;

/**
 * This test ensures that Randoop achieves a certain level of coverage
 * across 4 data structures. The coverage level that we check of is the
 * one published in the ICSE 2007 paper "Feedback-directed Random Test
 * Generation" (Section 3.1).
 * 
 * For each data structure, we expect Randoop to achieve the published
 * coverage in no longer than 2 minutes.
 * 
 * Note that this test does not constitute the experiment published in
 * the paper; it only checks that the achievable coverage number can be in
 * fact achieved by Randoop.
 * 
 */
public class ICSE07ContainersTest extends TestCase {

  ForwardGenerator explorer = null;

  public static void runRandoop(String name, List<Class<?>> classList,
      Pattern pattern, IStopper stopper) {

    System.out.println("ICSE 2006 container: " + name);

    List<StatementKind> statements = 
      Reflection.getStatements(classList, new DefaultReflectionFilter(pattern));
    
    ComponentManager componentMgr = new ComponentManager(SeedSequences.defaultSeeds());
    ForwardGenerator explorer = new ForwardGenerator(statements,
        120000 /* two minutes */, Integer.MAX_VALUE, componentMgr, stopper, null, null);
    explorer.setObjectCache(new ObjectCache(new EverythingIsDifferentMatcher()));
    GenInputsAbstract.maxsize = 10000; // Integer.MAX_VALUE;
    GenInputsAbstract.repeat_heuristic = true;
    ReflectionExecutor.usethreads  = false;
    randoop.Globals.nochecks = true;
    explorer.explore();
  }

  public static void testFibHeap() throws IOException {
    List<Class<?>> classList = new ArrayList<Class<?>>();
    classList.add(FibHeap.class);
    FibHeap.rand.setSeed(0);
    randoop.util.Randomness.reset(0);
    IStopper stopper = new IStopper() {
      @Override
      public boolean stop() {
        return FibHeap.tests.size() >= 96;
      }
    };
    runRandoop("FibHeap", classList, Pattern.compile("decreaseKey|delete\\(randoop.test.issta2006.Node\\)|empty()|insert\\(randoop.test.issta2006.Node\\)|min\\(\\)|size\\(\\)|union"), stopper);
    assertEquals(96, FibHeap.tests.size());
  }

  public static void testBinTree() {
    List<Class<?>> classList = new ArrayList<Class<?>>();
    classList.add(BinTree.class);
    randoop.util.Randomness.reset(0);
    IStopper stopper = new IStopper() {
      @Override
      public boolean stop() {
        return BinTree.tests.size() >= 54;
      }
    };
    runRandoop("BinTree", classList, Pattern.compile("find\\(int\\)|gen_native"), stopper);
    assertEquals(54, BinTree.tests.size());
  }

  public static void testTreeMap() {
    List<Class<?>> classList = new ArrayList<Class<?>>();
    classList.add(TreeMap.class);
    randoop.util.Randomness.reset(0);
    IStopper stopper = new IStopper() {
      @Override
      public boolean stop() {
        return TreeMap.tests.size() >= 106;
      }
    };
    runRandoop("TreeMap", classList, Pattern.compile("toString|size\\(\\)|containsKey\\(int\\)|print\\(\\)|concreteString\\(int\\)"), stopper);
    assertEquals(106, TreeMap.tests.size());
  }

  public static void testBinomialHeap() {
    List<Class<?>> classList = new ArrayList<Class<?>>();
    classList.add(BinomialHeap.class);
    randoop.util.Randomness.reset(0);
    IStopper stopper = new IStopper() {
      @Override
      public boolean stop() {
        return BinomialHeap.tests.size() >= 101;
      }
    };
    runRandoop("BinomialHeap", classList, Pattern.compile("findMinimum()"), stopper);
    assertEquals(101, randoop.test.issta2006.BinomialHeap.tests.size());
  }

}
