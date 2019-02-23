package randoop.test;

import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import junit.framework.TestCase;
import randoop.util.CollectionsExt;
import randoop.util.Randomness;
import randoop.util.Util;

public class UtilTests extends TestCase {

  private <T> List<T> makeList(int length) {
    List<T> result = new ArrayList<>(length);
    for (int j = 0; j < length; j++) {
      result.add(null);
    }
    assertEquals(length, result.size());
    return result;
  }

  public void testChunkUp1() throws Exception {
    List<String> list = makeList(243);
    int maxLength = 100;
    List<List<String>> chunks = CollectionsExt.formSublists(list, maxLength);
    assertEquals(3, chunks.size());
    assertEquals(100, chunks.get(0).size());
    assertEquals(100, chunks.get(1).size());
    assertEquals(43, chunks.get(2).size());
  }

  public void testChunkUp2() throws Exception {
    List<String> list = makeList(43);
    int maxLength = 100;
    List<List<String>> chunks = CollectionsExt.formSublists(list, maxLength);
    assertEquals(1, chunks.size());
    assertEquals(43, chunks.get(0).size());
  }

  public void testChunkUp3() throws Exception {
    List<String> list = makeList(0);
    int maxLength = 100;
    List<List<String>> chunks = CollectionsExt.formSublists(list, maxLength);
    assertEquals(0, chunks.size());
  }

  public void testChunkUp4() throws Exception {
    List<String> list = makeList(200);
    int maxLength = 100;
    List<List<String>> chunks = CollectionsExt.formSublists(list, maxLength);
    assertEquals(2, chunks.size());
    assertEquals(100, chunks.get(0).size());
    assertEquals(100, chunks.get(1).size());
  }

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
