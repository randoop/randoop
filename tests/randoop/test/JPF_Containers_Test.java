package randoop.test;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import randoop.ForwardGenerator;
import randoop.util.Reflection;

/** Runs randoop on four data structures and checks that a specific predicate
 * coverage was achieved.
 */
public class JPF_Containers_Test extends TestCase {

  public static void testBinomialHeap() {
    List<Class<?>> l = new ArrayList<Class<?>>();
    l.add(randoop.test.issta2006.BinomialHeap.class);
    ForwardGenerator explorer =
      new ForwardGenerator(Reflection.getStatements(l, null), null, Long.MAX_VALUE, 100, null, null);
    explorer.explore();
    //assertEquals(36, BinomialHeap.counter);
  }

  public static void testBinTree() {
    List<Class<?>> l = new ArrayList<Class<?>>();
    l.add(randoop.test.issta2006.BinTree.class);
    ForwardGenerator explorer =
      new ForwardGenerator(Reflection.getStatements(l, null), null, Long.MAX_VALUE, 100, null, null);
    explorer.explore();
    //assertEquals(32, BinTree.counter);
  }

  public static void testFibHeap() {
    List<Class<?>> l = new ArrayList<Class<?>>();
    l.add(randoop.test.issta2006.FibHeap.class);
    ForwardGenerator explorer =
      new ForwardGenerator(Reflection.getStatements(l, null), null, Long.MAX_VALUE, 100, null, null);
    explorer.explore();
    //assertEquals(91, FibHeap.counter);
  }

  public static void testTreeMap() {
    List<Class<?>> l = new ArrayList<Class<?>>();
    l.add(randoop.test.issta2006.TreeMap.class);
    ForwardGenerator explorer =
      new ForwardGenerator(Reflection.getStatements(l, null), null, Long.MAX_VALUE, 100, null, null);
    explorer.explore();
    //assertEquals(32, TreeMap.counter);
  }
}
