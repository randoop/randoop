package randoop.test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import randoop.Globals;
import randoop.main.TypeReader;
import randoop.main.GenInputsAbstract;
import randoop.operation.Operation;
import randoop.reflection.OperationExtractor;
import randoop.sequence.ForwardGenerator;
import randoop.util.Timer;

import junit.framework.TestCase;

// DEPRECATED. Will delete after testing other performance tests
// in different machines.
public class ForwardExplorerPerformanceTest extends TestCase {

  private static final int TIME_LIMIT_SECS = 10;
  private static final long EXPECTED_MIN = 18000000 / performanceMultiplier();

  private static long performanceMultiplier() {
    String foo = "make sure that the loop doesn't get optimized away";
    List<String> list = new ArrayList<String>();
    Timer t = new Timer();
    t.startTiming();
    for (int i = 0; i < 10000000; i++) {
      list.add(foo);
      list.remove(0);
    }
    return t.getTimeElapsedMillis();
  }

  @SuppressWarnings("unchecked")
  public static void test1() {

    String resourcename = "resources/java.util.classlist.java1.6.txt";

    InputStream classStream =
      ForwardExplorerPerformanceTest.class.getResourceAsStream(resourcename);

    List<Operation> model =
      OperationExtractor.getOperations(TypeReader.getTypesForStream(classStream, resourcename),null);
    System.out.println("done creating model.");
    GenInputsAbstract.dontexecute = true; // FIXME make this an instance field?
    GenInputsAbstract.debug_checks = false;
    ForwardGenerator explorer = new ForwardGenerator(model, TIME_LIMIT_SECS*1000, Integer.MAX_VALUE, null, null, null, null);
    System.out.println("" + Globals.lineSep + "Will explore for " + TIME_LIMIT_SECS + " seconds.");
    explorer.explore();
    System.out.println("" + Globals.lineSep + "" + Globals.lineSep + "Expected " + EXPECTED_MIN + " sequences, created " + explorer.allSequences.size() + " sequences.");
    GenInputsAbstract.dontexecute = false;
    GenInputsAbstract.debug_checks = true;
    if (explorer.allSequences.size() < EXPECTED_MIN) {
      StringBuilder b = new StringBuilder();
      b.append("Randoop's explorer created fewer than " + EXPECTED_MIN);
      b.append(" inputs (precisely, " + explorer.allSequences.size()+ ") in ");
      b.append(TIME_LIMIT_SECS + " seconds." + Globals.lineSep + "");
      b.append("This failure could have two causes:" + Globals.lineSep + "");
      b.append(" (1) Our guess as to how fast your machine is is wrong." + Globals.lineSep + "");
      b.append(" (2) You made a change to Randoop that slows down its performance." + Globals.lineSep + "");
      b.append("     No tips available here.");
      fail(b.toString());
    }

  }
}
