package randoop.test;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import randoop.Globals;
import randoop.main.OptionsCache;
import randoop.util.Timer;

public abstract class AbstractPerformanceTest {

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

  abstract int expectedTimeMillis();

  abstract void execute();

  private final double DIVIDE_FACTOR = 1700;

  private final double EXPECTED_MIN = (0.7) / computeFactor();

  private double computeFactor() {
    String foo = "make sure that the loop doesn't get optimized away";
    List<String> list = new ArrayList<>();
    Timer t = new Timer();
    t.startTiming();
    for (int i = 0; i < 50000000; i++) {
      list.add(foo);
      list.remove(0);
    }
    t.stopTiming();
    return t.getTimeElapsedMillis() / DIVIDE_FACTOR;
  }

  @Test
  @SuppressWarnings("unchecked")
  public void test() {

    System.out.println("@@@ factor: " + computeFactor());
    double factor = EXPECTED_MIN;
    System.out.println("@@@ EXPECTED_MIN: " + EXPECTED_MIN);
    long expected = (long) (factor * expectedTimeMillis());

    Timer timer = new Timer();
    timer.startTiming();
    execute();
    timer.stopTiming();

    System.out.println();
    System.out.println("Expected time: " + expected);
    System.out.println("Actual time:   " + timer.getTimeElapsedMillis());

    if (timer.getTimeElapsedMillis() > expected) {
      StringBuilder b = new StringBuilder();
      b.append(
          "Failure: performance test actual time was greater than expected time."
              + Globals.lineSep);
      b.append("This failure could have two causes:" + Globals.lineSep + "");
      b.append(" (1) Our guess as to how fast your machine is is wrong." + Globals.lineSep + "");
      b.append(
          " (2) You made a change to Randoop that slows down its performance."
              + Globals.lineSep
              + "");
      fail(b.toString());
    }
  }
}
