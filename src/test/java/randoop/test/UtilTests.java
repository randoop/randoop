package randoop.test;

import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import junit.framework.TestCase;
import randoop.util.Randomness;
import randoop.util.Util;

public class UtilTests extends TestCase {

  public void testGetRandomSetMemeber1() throws Exception {
    Set<Integer> ints = new LinkedHashSet<>(Arrays.asList(1, 3, 4));
    Integer random = Randomness.randomSetMember(ints);
    assertTrue(random != null);
    assertTrue(ints.contains(random));
  }

  public void testHex1() throws Exception {
    String s = "x";
    assertEquals("\u0078", s);

    String hex = Util.convertToHexString(s);
    assertEquals("\\u0078", hex);
  }

  public void testHex2() throws Exception {
    String s = new DecimalFormatSymbols().getInfinity();

    assertEquals("\u221E", s);

    String hex = Util.convertToHexString(s);
    assertEquals("\\u221e", hex);
  }

  public void testOccurCount1() throws Exception {
    StringBuilder text = new StringBuilder("foo foo");
    String s = "foo";

    assertEquals(2, Util.occurCount(text, s));
  }

  public void testOccurCount2() throws Exception {
    StringBuilder text = new StringBuilder("foo");
    String s = "foo";

    assertEquals(1, Util.occurCount(text, s));
  }

  public void testOccurCount3() throws Exception {
    StringBuilder text = new StringBuilder("bar");
    String s = "foo";

    assertEquals(0, Util.occurCount(text, s));
  }

  public void testOccurCount4() throws Exception {
    StringBuilder text = new StringBuilder("");
    String s = "foo";

    assertEquals(0, Util.occurCount(text, s));
  }

  public void testOccurCount5() throws Exception {
    StringBuilder text = new StringBuilder("bababa");
    String s = "aba";

    assertEquals(2, Util.occurCount(text, s));
  }

  public void testOccurCount6() throws Exception {
    StringBuilder text = new StringBuilder("aa");
    String s = "a";

    assertEquals(2, Util.occurCount(text, s));
  }

  public void testOccurCount7() throws Exception {
    StringBuilder text = new StringBuilder("aaa");
    String s = "aa";

    assertEquals(2, Util.occurCount(text, s));
  }

  @SuppressWarnings("unused") // debugging
  private void printAll(Map<Integer, Set<Object[]>> name) {
    for (int x : name.keySet()) {
      for (Object[] objs : name.get(x)) {
        System.out.println(Arrays.toString(objs));
      }
    }
  }
}
