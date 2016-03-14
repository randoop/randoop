package randoop.test;

import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
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
    List<T> result = new ArrayList<T>(length);
    for (int j = 0; j < length; j++) {
      result.add(null);
    }
    assertEquals(length, result.size());
    return result;
  }

  public void testChunkUp1() throws Exception {
    List<String> list = makeList(243);
    int maxLength = 100;
    List<List<String>> chunks = CollectionsExt.chunkUp(list, maxLength);
    assertEquals(3, chunks.size());
    assertEquals(100, chunks.get(0).size());
    assertEquals(100, chunks.get(1).size());
    assertEquals(43, chunks.get(2).size());
  }

  public void testChunkUp2() throws Exception {
    List<String> list = makeList(43);
    int maxLength = 100;
    List<List<String>> chunks = CollectionsExt.chunkUp(list, maxLength);
    assertEquals(1, chunks.size());
    assertEquals(43, chunks.get(0).size());
  }

  public void testChunkUp3() throws Exception {
    List<String> list = makeList(0);
    int maxLength = 100;
    List<List<String>> chunks = CollectionsExt.chunkUp(list, maxLength);
    assertEquals(0, chunks.size());
  }

  public void testChunkUp4() throws Exception {
    List<String> list = makeList(200);
    int maxLength = 100;
    List<List<String>> chunks = CollectionsExt.chunkUp(list, maxLength);
    assertEquals(2, chunks.size());
    assertEquals(100, chunks.get(0).size());
    assertEquals(100, chunks.get(1).size());
  }

  public void testGetRandomSetMemeber1() throws Exception {
    Set<Integer> ints = new LinkedHashSet<Integer>(Arrays.asList(1, 3, 4));
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

  public void testUniqueList1() {
    List<Integer> lst = Arrays.asList(1, 2, 3, 4);
    assertEquals(lst, CollectionsExt.unique(lst));
  }

  public void testUniqueList2() {
    List<Integer> lst = Arrays.asList(1, 2, 1, 4);
    List<Integer> lstU = Arrays.asList(1, 2, 4);
    assertEquals(lstU, CollectionsExt.unique(lst));
  }

  public void testRemoveMatching1() {
    List<String> lst = new ArrayList<String>(Arrays.asList("foo", "bar", "baz"));
    List<String> expected = Arrays.asList("foo");
    assertEquals(expected, CollectionsExt.removeMatching("ba.", lst));
  }

  public void testJoin1() {
    List<String> lst = Arrays.asList("foo", "bar", "baz");
    assertEquals("fooXbarXbaz", CollectionsExt.join("X", lst));
  }

  @SuppressWarnings("unchecked")
  public void testAllCombinations() throws Exception {
    List<List<String>> all =
        CollectionsExt.allCombinations(
            Arrays.<List<String>>asList(
                Arrays.<String>asList("1", "2"), Arrays.<String>asList("a", "b")));
    assertEquals(all.toString(), 4, all.size());
  }

  @SuppressWarnings("unchecked")
  public void testAllCombinations2() throws Exception {
    List<List<String>> all =
        CollectionsExt.allCombinations(
            Arrays.<List<String>>asList(Arrays.<String>asList("1", "2")));
    assertEquals(all.toString(), 2, all.size());
  }

  @SuppressWarnings("unchecked")
  public void testAllCombinations3() throws Exception {
    List<List<String>> all =
        CollectionsExt.allCombinations(
            Arrays.<List<String>>asList(
                Arrays.<String>asList("1", "2"),
                Arrays.<String>asList("a", "b", "c"),
                Arrays.<String>asList("q", "w", "e", "r")));
    assertEquals(all.toString(), 2 * 3 * 4, all.size());
  }

  @SuppressWarnings("unchecked")
  public void testAllCombinations4() throws Exception {
    List<List<String>> all =
        CollectionsExt.allCombinations(
            Arrays.<List<String>>asList(Arrays.<String>asList("1", "2")));
    assertEquals(all.toString(), 2, all.size());
  }

  @SuppressWarnings("unchecked")
  public void testAllCombinations5() throws Exception {
    List<List<String>> all =
        CollectionsExt.allCombinations(Arrays.<List<String>>asList(Arrays.<String>asList()));
    assertEquals(all.toString(), 0, all.size());
  }

  @SuppressWarnings("unchecked")
  public void testAllCombinations6() throws Exception {
    List<List<String>> all = CollectionsExt.allCombinations(Arrays.<List<String>>asList());
    assertEquals(all.toString(), 1, all.size());
  }

  public void testcreatePerArityGroups1() throws Exception {
    Map<Integer, Set<Object[]>> name =
        CollectionsExt.createPerArityGroups(new Object[] {"a", "b"}, 2);
    assertEquals(7, totalArrayCount(name));
  }

  public void testcreatePerArityGroups2() throws Exception {
    Map<Integer, Set<Object[]>> name =
        CollectionsExt.createPerArityGroups(new Object[] {"a", "b"}, 3);
    assertEquals(15, totalArrayCount(name));
  }

  public void testcreatePerArityGroups3() throws Exception {
    Map<Integer, Set<Object[]>> name =
        CollectionsExt.createPerArityGroups(new Object[] {"a", "b", "c"}, 2);
    assertEquals(13, totalArrayCount(name));
  }

  public void testcreatePerArityGroups4() throws Exception {
    Map<Integer, Set<Object[]>> name =
        CollectionsExt.createPerArityGroups(new Object[] {"a", "b", "c"}, 3);
    //  printAll(name);
    assertEquals(40, totalArrayCount(name));
  }

  @SuppressWarnings("unused") //debugging
  private void printAll(Map<Integer, Set<Object[]>> name) {
    for (int x : name.keySet()) {
      for (Object[] objs : name.get(x)) {
        System.out.println(Arrays.toString(objs));
      }
    }
  }

  private int totalArrayCount(Map<Integer, Set<Object[]>> map) {
    int result = 0;
    for (int x : map.keySet()) {
      result += map.get(x).size();
    }
    return result;
  }

  public void testMap() throws Exception {
    List<Integer> ints = Arrays.asList(3, 4, 5);
    Map<Integer, Integer> m = new LinkedHashMap<Integer, Integer>();
    m.put(3, 9);
    m.put(4, 16);
    m.put(5, 25);
    List<Integer> result = CollectionsExt.map(ints, m);
    assertEquals(Arrays.asList(9, 16, 25), result);
  }
}
