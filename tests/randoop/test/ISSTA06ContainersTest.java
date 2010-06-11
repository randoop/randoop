package randoop.test;



import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import junit.framework.TestCase;
import randoop.EverythingIsDifferentMatcher;
import randoop.ForwardGenerator;
import randoop.ObjectCache;
import randoop.StatementKind;
import randoop.main.GenInputsAbstract;
import randoop.test.issta2006.BinTree;
import randoop.test.issta2006.FibHeap;
import randoop.util.DefaultReflectionFilter;
import randoop.util.Randomness;
import randoop.util.Reflection;
import randoop.util.ReflectionExecutor;


public class ISSTA06ContainersTest extends TestCase {

  ForwardGenerator explorer = null;

  public static void runRandoop(String name, List<Class<?>> classList, int inputLimit,
      Pattern pattern) {


    Randomness.reset(System.currentTimeMillis());

    System.out.println("ISSTA06 Containers experiment: " + name);

    List<StatementKind> statements = 
      Reflection.getStatements(classList, new DefaultReflectionFilter(pattern));

    ForwardGenerator explorer = new ForwardGenerator(statements,
        null, Long.MAX_VALUE, inputLimit, null, null);
    explorer.setObjectCache(new ObjectCache(new EverythingIsDifferentMatcher()));
    GenInputsAbstract.maxsize = 10000; // Integer.MAX_VALUE;
    GenInputsAbstract.repeat_heuristic = true;
    //GenFailures.clear = 1000;
    //GenFailures.noprogressdisplay = true;
    ReflectionExecutor.usethreads  = false;
    randoop.Globals.nochecks = true;
    System.out.println("done. Starting exploration.");
    explorer.explore();
  }

  public static void testFibHeap() {
    List<Class<?>> classList = new ArrayList<Class<?>>();
    classList.add(FibHeap.class);
    FibHeap.rand.setSeed(0);
    randoop.util.Randomness.reset(0);
    runRandoop("FibHeap", classList, 2200, Pattern.compile("decreaseKey|delete\\(randoop.test.issta2006.Node\\)|empty()|insert\\(randoop.test.issta2006.Node\\)|min\\(\\)|size\\(\\)|union"));
    assertEquals(96, FibHeap.tests.size());
  }

  public static void testBinTree() {
    List<Class<?>> classList = new ArrayList<Class<?>>();
    classList.add(BinTree.class);
    randoop.util.Randomness.reset(0);
    runRandoop("BinTree", classList, 1000, Pattern.compile("find\\(int\\)|gen_native"));
    assertEquals(54, BinTree.tests.size());
  }

  public static void testTreeMap() {
    List<Class<?>> classList = new ArrayList<Class<?>>();
    classList.add(randoop.test.issta2006.TreeMap.class);
    randoop.util.Randomness.reset(0);
    runRandoop("TreeMap", classList, 5000, Pattern.compile("toString|size\\(\\)|containsKey\\(int\\)|print\\(\\)|concreteString\\(int\\)"));
    assertEquals(106, randoop.test.issta2006.TreeMap.tests.size());
  }

  public static void testBinomialHeap() {
    List<Class<?>> classList = new ArrayList<Class<?>>();
    classList.add(randoop.test.issta2006.BinomialHeap.class);
    randoop.util.Randomness.reset(0);
    runRandoop("BinomialHeap", classList, 4300, Pattern.compile("findMinimum()"));
    assertEquals(101, randoop.test.issta2006.BinomialHeap.tests.size());
  }

}
