package randoop.test;

import java.lang.reflect.Method;
import java.math.RoundingMode;
import java.text.BreakIterator;
import java.text.NumberFormat;
import java.util.ArrayList;

import junit.framework.TestCase;
import randoop.util.DefaultReflectionFilter;

public class DefaultReflectionFilterTests extends TestCase{
  private DefaultReflectionFilter filter;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    filter = new DefaultReflectionFilter(null);
  }

  public void testObjectMethods1() throws Exception {
    Method wait= Object.class.getMethod("wait", new Class[0]);
    assertTrue(! filter.canUse(wait));
  }
  public void testObjectMethods2() throws Exception {
    Method wait1= Object.class.getMethod("wait", new Class<?>[]{long.class});
    assertTrue(! filter.canUse(wait1));
  }
  public void testObjectMethods3() throws Exception {
    Method wait2= Object.class.getMethod("wait", new Class<?>[]{long.class, int.class});
    assertTrue(! filter.canUse(wait2));
  }
  public void testObjectMethods4() throws Exception {
    Method notify= Object.class.getMethod("notify", new Class[0]);
    assertTrue(! filter.canUse(notify));
  }
  public void testObjectMethods5() throws Exception {
    Method notifyAll= Object.class.getMethod("notifyAll", new Class[0]);
    assertTrue(! filter.canUse(notifyAll));
  }
  public void testObjectMethods6() throws Exception {
    Method hashCode= Object.class.getMethod("hashCode", new Class[0]);
    assertTrue(! filter.canUse(hashCode));
  }
  public void testObjectMethods7() throws Exception {
    Method toString= Object.class.getMethod("toString", new Class[0]);
    assertTrue(! filter.canUse(toString));
  }

  public void testSyntheticMethods() throws Exception {
    Method wait= Long.class.getMethod("compareTo", new Class<?>[]{Object.class});
    assertTrue(! filter.canUse(wait));
  }

  public void testComparetoInEnums() throws Exception {
    Method wait= RoundingMode.class.getMethod("compareTo", new Class<?>[]{Enum.class});
    assertTrue(! filter.canUse(wait));
  }

  //this could be OK - currently we disable it
  public void testObjectMethods8() throws Exception {
    Method equals= Object.class.getMethod("equals", new Class<?>[]{Object.class});
    assertTrue(! filter.canUse(equals));
  }

  //this could be OK - currently we disable it
  public void testObjectMethods9() throws Exception {
    Method getClass= Object.class.getMethod("getClass", new Class[0]);
    assertTrue(! filter.canUse(getClass));
  }

  public void testNondeterministicHashCode() throws Exception {
    Method wait= ArrayList.class.getMethod("hashCode", new Class[0]);
    assertTrue(! filter.canUse(wait));
  }

  public void testGetAvailableLocales1() throws Exception {
    Method wait= NumberFormat.class.getMethod("getAvailableLocales", new Class[0]);
    assertTrue(! filter.canUse(wait));
  }

  //this method appears more than once in the JDK
  public void testGetAvailableLocales2() throws Exception {
    Method wait= BreakIterator.class.getMethod("getAvailableLocales", new Class[0]);
    assertTrue(! filter.canUse(wait));
  }

  //-------------- these are OK to use -------------------

  public void testLong() throws Exception {
    Method wait= Long.class.getMethod("compareTo", new Class<?>[]{Long.class});
    assertTrue(filter.canUse(wait));
  }

  //it's OK to exercise this one
  public void testStringHashCode() throws Exception {
    Method wait= String.class.getMethod("hashCode", new Class[0]);
    assertTrue(filter.canUse(wait));
  }

}
