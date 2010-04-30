package randoop.main;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import randoop.EverythingIsDifferentMatcher;
import randoop.ForwardGenerator;
import randoop.ObjectCache;
import randoop.StatementKind;
import randoop.test.issta2006.BinTree;
import randoop.test.issta2006.FibHeap;
import randoop.util.DefaultReflectionFilter;
import randoop.util.Randomness;
import randoop.util.Reflection;
import randoop.util.ReflectionExecutor;



public class RunISSTA06Containers extends CommandHandler {

  public RunISSTA06Containers() {
    super("issta-containers", "", "", "", "", null, "", "", "", null);
  }

  @Override
  public boolean handle(String[] args) throws RandoopTextuiException {

    Class<?> subject = null;

    try {
      subject = Class.forName(args[0]);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }

    boolean directed = false;
    String directedStr = args[1];
    if (directedStr.equals("directed")) {
      directed = true;
    } else if (directedStr.equals("undirected")) {
      directed = false;
    } else {
      throw new RuntimeException(directedStr);
    }

    randoop.util.Randomness.reset(System.currentTimeMillis());

    if (subject.equals(FibHeap.class)) {
      int timelimit = 120000;
      testFibHeap(timelimit, directed);
      System.out.println("TestsCovered:" + FibHeap.tests.size());
    } else if (subject.equals(BinTree.class)) {
      int timelimit = 120000;
      testBinTree(timelimit, directed);
      System.out.println("TestsCovered:" + BinTree.tests.size());
    } else if (subject.equals(randoop.test.issta2006.TreeMap.class)) {
      int timelimit = 120000;
      testTreeMap(timelimit, directed);
      System.out.println("TestsCovered:" + randoop.test.issta2006.TreeMap.tests.size());
    } else if (subject.equals(randoop.test.issta2006.BinomialHeap.class)) {
      int timelimit = 120000;
      testBinomialHeap(timelimit, directed);
      System.out.println("TestsCovered:" + randoop.test.issta2006.BinomialHeap.tests.size());
    } else throw new RuntimeException(subject.toString());

    return true;
  }


  public static void runRandoop(List<Class<?>> classList, int timeLimit, Pattern pattern, boolean directed) {

    Randomness.reset(System.currentTimeMillis());

    List<StatementKind> statements = 
      Reflection.getStatements(classList, new DefaultReflectionFilter(pattern));

    ForwardGenerator explorer = new ForwardGenerator(statements,
        null, timeLimit, Integer.MAX_VALUE, null);
    explorer.setObjectCache(new ObjectCache(new EverythingIsDifferentMatcher()));
    GenInputsAbstract.maxsize = 10000; // Integer.MAX_VALUE;
    GenInputsAbstract.noprogressdisplay = true;
    if (directed) {
      GenInputsAbstract.repeat_heuristic = true;
      // All other heuristics are true by default, so nothing to set.
    } else {
      GenInputsAbstract.dont_check_contracts = true;
      GenInputsAbstract.forbid_null = false;
      GenInputsAbstract.repeat_heuristic = false;
    }

    ReflectionExecutor.usethreads = false;
    randoop.Globals.nochecks = true;
    System.out.println("done. Starting exploration.");
    long startTime = System.currentTimeMillis();
    explorer.explore();
    System.out.println("Execution time:" + (System.currentTimeMillis() - startTime)/1000);
  }


  public static void testFibHeap(int limit, boolean directed) {
    List<Class<?>> classList = new ArrayList<Class<?>>();
    classList.add(FibHeap.class);
    FibHeap.rand.setSeed(System.currentTimeMillis());
    runRandoop(classList, limit, Pattern.compile("decreaseKey|delete\\(randoop.test.issta2006.Node\\)|empty()|insert\\(randoop.test.issta2006.Node\\)|min\\(\\)|size\\(\\)|union"), directed);
  }

  public static void testBinTree(int limit, boolean directed) {
    List<Class<?>> classList = new ArrayList<Class<?>>();
    classList.add(BinTree.class);
    runRandoop(classList, limit, Pattern.compile("find\\(int\\)|gen_native"), directed);
  }

  public static void testTreeMap(int limit, boolean directed) {
    List<Class<?>> classList = new ArrayList<Class<?>>();
    classList.add(randoop.test.issta2006.TreeMap.class);
    runRandoop(classList, limit, Pattern.compile("toString|size\\(\\)|containsKey\\(int\\)|print\\(\\)|concreteString\\(int\\)"), directed);
  }

  public static void testBinomialHeap(int limit, boolean directed) {
    List<Class<?>> classList = new ArrayList<Class<?>>();
    classList.add(randoop.test.issta2006.BinomialHeap.class);
    runRandoop(classList, limit, Pattern.compile("findMinimum()"), directed);
  }

}
