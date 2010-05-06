package randoop.test;


import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import randoop.ForwardGenerator;
import randoop.ObjectCache;
import randoop.StatementKind;
import randoop.main.GenInputsAbstract;
import randoop.util.HeapShapeMatcher;
import randoop.util.Randomness;
import randoop.util.Reflection;
import randoop.util.ReflectionExecutor;

// THIS TEST WILL RESULT IN NONDETERMINISM IN THE UNIT TEST BECAUSE IT
// USES A TIME LMIT, NOT AN INPUT LIMIT. BEFORE ADDING BACK TO UNIT
// TESTS, REMOVE NONDETERMINISM.
public class SymstraContainersTest extends TestCase {

  ForwardGenerator explorer = null;


  public void setup() {

  }

  public static void testUBStack() {

    System.out.println("Symstra experiment: UBStack");

    List<Class<?>> classList = new ArrayList<Class<?>>();
    classList.add(randoop.test.symexamples.UBStack.class);

    List<StatementKind> statements = 
      Reflection.getStatements(classList, null);

    ForwardGenerator explorer = new ForwardGenerator(statements,
        null, 3000, Integer.MAX_VALUE, null);

    ObjectCache cache = new ObjectCache(new HeapShapeMatcher());
    explorer.setObjectCache(cache);
    GenInputsAbstract.maxsize = 10000; // Integer.MAX_VALUE;
    //GenFailures.clear = 1000;
    //GenFailures.noprogressdisplay = true;
    ReflectionExecutor.usethreads = false;
    randoop.Globals.nochecks = true;
    Randomness.reset(System.currentTimeMillis());
    System.out.println("done. Starting exploration.");
    explorer.explore();
    for (Class<?> cls : classList) {
      System.out.println(randoop.util.TestCoverageInfo.getCoverageInfo(cls));
    }
  }



}
