package randoop.test;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import plume.EntryReader;
import randoop.Globals;
import randoop.generation.ForwardGenerator;
import randoop.main.GenInputsAbstract;
import randoop.main.OptionsCache;
import randoop.operation.TypedOperation;
import randoop.reflection.DefaultReflectionPredicate;
import randoop.reflection.ModelCollections;
import randoop.reflection.OperationExtractor;
import randoop.reflection.PublicVisibilityPredicate;
import randoop.reflection.ReflectionManager;
import randoop.reflection.TypedOperationManager;
import randoop.types.ClassOrInterfaceType;
import randoop.util.Timer;

import static org.junit.Assert.fail;

// DEPRECATED. Will delete after testing other performance tests
// in different machines.
public class ForwardExplorerPerformanceTest {

  private static final int TIME_LIMIT_SECS = 10;
  private static final long EXPECTED_MIN = 18000000 / performanceMultiplier();

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

  private static long performanceMultiplier() {
    String foo = "make sure that the loop doesn't get optimized away";
    List<String> list = new ArrayList<>();
    Timer t = new Timer();
    t.startTiming();
    for (int i = 0; i < 10000000; i++) {
      list.add(foo);
      list.remove(0);
    }
    return t.getTimeElapsedMillis();
  }

  @Test
  @SuppressWarnings("unchecked")
  public void test1() {

    String resourcename = "java.util.classlist.java1.6.txt";

    final List<TypedOperation> model = new ArrayList<>();
    TypedOperationManager operationManager = new TypedOperationManager(new ModelCollections() {
      @Override
      public void addConcreteOperation(ClassOrInterfaceType declaringType, TypedOperation operation) {
        model.add(operation);
      }
    });
    ReflectionManager manager = new ReflectionManager(new PublicVisibilityPredicate());
    manager.add(new OperationExtractor(operationManager, new DefaultReflectionPredicate()));

    try (EntryReader er = new EntryReader(ForwardExplorerPerformanceTest.class.getResourceAsStream(resourcename))) {
      for (String entry : er) {
        manager.apply(Class.forName(entry));
      }
    } catch (IOException e) {
      fail("exception when reading class names " + e);
    } catch (ClassNotFoundException e) {
      fail("class not found when reading classnames: " + e);
    }

    System.out.println("done creating model.");
    GenInputsAbstract.dontexecute = true; // FIXME make this an instance field?
    GenInputsAbstract.debug_checks = false;
    ForwardGenerator explorer =
        new ForwardGenerator(
            model, new LinkedHashSet<TypedOperation>(), TIME_LIMIT_SECS * 1000, Integer.MAX_VALUE, Integer.MAX_VALUE, null, null, null);
    System.out.println("" + Globals.lineSep + "Will explore for " + TIME_LIMIT_SECS + " seconds.");
    explorer.explore();
    System.out.println(
        ""
            + Globals.lineSep
            + ""
            + Globals.lineSep
            + "Expected "
            + EXPECTED_MIN
            + " sequences, created "
            + explorer.numGeneratedSequences()
            + " sequences.");
    GenInputsAbstract.dontexecute = false;
    GenInputsAbstract.debug_checks = true;
    if (explorer.numGeneratedSequences() < EXPECTED_MIN) {
      String b = "Randoop's explorer created fewer than " + EXPECTED_MIN
              + " inputs (precisely, " + explorer.numGeneratedSequences() + ") in "
              + TIME_LIMIT_SECS + " seconds." + Globals.lineSep
              + "This failure could have two causes:" + Globals.lineSep
              + " (1) Our guess as to how fast your machine is is wrong." + Globals.lineSep
              + " (2) You made a change to Randoop that slows down its performance." + Globals.lineSep
              + "     No tips available here.";
      fail(b);
    }
  }
}
