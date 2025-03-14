package randoop.test;

import java.lang.reflect.Method;
import java.math.RoundingMode;
import java.text.BreakIterator;
import java.text.NumberFormat;
import java.util.ArrayList;
import junit.framework.TestCase;
import randoop.reflection.DefaultReflectionPredicate;

public class DefaultReflectionPredicateTests extends TestCase {
  private DefaultReflectionPredicate filter;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    filter = new DefaultReflectionPredicate();
  }

  public void testObjectMethods1() throws Exception {
    Method wait = Object.class.getMethod("wait", new Class<?>[0]);
    assertFalse(filter.test(wait));
  }

  public void testObjectMethods2() throws Exception {
    Method wait1 = Object.class.getMethod("wait", new Class<?>[] {long.class});
    assertFalse(filter.test(wait1));
  }

  public void testObjectMethods3() throws Exception {
    Method wait2 = Object.class.getMethod("wait", new Class<?>[] {long.class, int.class});
    assertFalse(filter.test(wait2));
  }

  public void testObjectMethods4() throws Exception {
    Method notify = Object.class.getMethod("notify", new Class<?>[0]);
    assertFalse(filter.test(notify));
  }

  public void testObjectMethods5() throws Exception {
    Method notifyAll = Object.class.getMethod("notifyAll", new Class<?>[0]);
    assertFalse(filter.test(notifyAll));
  }

  public void testObjectMethods6() throws Exception {
    Method hashCode = Object.class.getMethod("hashCode", new Class<?>[0]);
    assertFalse(filter.test(hashCode));
  }

  public void testObjectMethods7() throws Exception {
    Method toString = Object.class.getMethod("toString", new Class<?>[0]);
    assertFalse(filter.test(toString));
  }

  public void testSyntheticMethods() throws Exception {
    Method wait = Long.class.getMethod("compareTo", new Class<?>[] {Object.class});
    assertFalse(filter.test(wait));
  }

  public void testComparetoInEnums() throws Exception {
    Method wait = RoundingMode.class.getMethod("compareTo", new Class<?>[] {Enum.class});
    assertFalse(filter.test(wait));
  }

  // equals is used in contracts, but not as operation in a sequence
  public void testObjectMethods8() throws Exception {
    Method equals = Object.class.getMethod("equals", new Class<?>[] {Object.class});
    assertFalse(filter.test(equals));
  }

  public void testObjectMethods9() throws Exception {
    Method getClass = Object.class.getMethod("getClass", new Class<?>[0]);
    assertTrue(filter.test(getClass));
  }

  public void testNondeterministicHashCode() throws Exception {
    Method wait = ArrayList.class.getMethod("hashCode", new Class<?>[0]);
    assertFalse(filter.test(wait));
  }

  public void testGetAvailableLocales1() throws Exception {
    Method wait = NumberFormat.class.getMethod("getAvailableLocales", new Class<?>[0]);
    assertFalse(filter.test(wait));
  }

  // this method appears more than once in the JDK
  public void testGetAvailableLocales2() throws Exception {
    Method wait = BreakIterator.class.getMethod("getAvailableLocales", new Class<?>[0]);
    assertFalse(filter.test(wait));
  }

  // -------------- these are OK to use -------------------

  public void testLong() throws Exception {
    Method wait = Long.class.getMethod("compareTo", new Class<?>[] {Long.class});
    assertTrue(filter.test(wait));
  }

  // it's OK to exercise this one
  public void testStringHashCode() throws Exception {
    Method wait = String.class.getMethod("hashCode", new Class<?>[0]);
    assertTrue(filter.test(wait));
  }
}
