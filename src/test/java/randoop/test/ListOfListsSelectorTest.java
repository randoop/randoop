package randoop.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.Test;
import randoop.util.ListOfLists;
import randoop.util.SimpleArrayList;
import randoop.util.SimpleList;

public class ListOfListsSelectorTest {

  static ListOfLists<Integer> empty;
  private static ListOfLists<Integer> l1;
  private static ListOfLists<Integer> l3;
  private static ListOfLists<Integer> l1l2;
  private static ListOfLists<Integer> l1l2l3;
  private static ListOfLists<Integer> l3l3l1l2;

  @BeforeClass
  public static void setUp() throws Exception {

    SimpleArrayList<Integer> l1List = new SimpleArrayList<>();
    SimpleArrayList<Integer> l2List = new SimpleArrayList<>();
    l2List.add(1);
    SimpleArrayList<Integer> l3List = new SimpleArrayList<>();
    l3List.add(2);
    l3List.add(3);

    List<SimpleList<Integer>> emptyList = new ArrayList<>();
    empty = new ListOfLists<>(emptyList);

    List<SimpleList<Integer>> l1ListList = new ArrayList<>();
    l1ListList.add(l1List);
    l1 = new ListOfLists<>(l1ListList);

    List<SimpleList<Integer>> l3ListList = new ArrayList<>();
    l3ListList.add(l3List);
    l3 = new ListOfLists<>(l3ListList);

    List<SimpleList<Integer>> l1l2ListList = new ArrayList<>();
    l1l2ListList.add(l1List);
    l1l2ListList.add(l2List);
    l1l2 = new ListOfLists<>(l1l2ListList);

    List<SimpleList<Integer>> l1l2l3ListList = new ArrayList<>();
    l1l2l3ListList.add(l1List);
    l1l2l3ListList.add(l2List);
    l1l2l3ListList.add(l3List);
    l1l2l3 = new ListOfLists<>(l1l2l3ListList);

    List<SimpleList<Integer>> l3l3l1l2ListList = new ArrayList<>();
    l3l3l1l2ListList.add(l3List);
    l3l3l1l2ListList.add(l3List);
    l3l3l1l2ListList.add(l1List);
    l3l3l1l2ListList.add(l2List);
    l3l3l1l2 = new ListOfLists<>(l3l3l1l2ListList);
  }

  /** Test method for 'randoop.util.ListOfLists.size()' */
  @Test
  public void testSize() {
    assertEquals(empty.size(), 0);
    assertEquals(l1.size(), 0);
    assertEquals(l3.size(), 2);
    assertEquals(l1l2.size(), 1);
    assertEquals(l1l2l3.size(), 3);
    assertEquals(l3l3l1l2.size(), 5);
  }

  private static void callGetElementShouldFail(ListOfLists<?> s, int i) {
    try {
      s.get(i);
      fail("Should raise an IllegalArgumentException");
    } catch (IllegalArgumentException expected) {
      assertTrue(true);
    }
  }

  /** Test method for 'randoop.util.ListOfLists.get(int)' */
  @Test
  public void testGetElement() {

    callGetElementShouldFail(empty, 0);
    callGetElementShouldFail(empty, -1);
    callGetElementShouldFail(empty, 1);

    callGetElementShouldFail(l1, 0);
    callGetElementShouldFail(l1, -1);
    callGetElementShouldFail(l1, 1);

    callGetElementShouldFail(l3, -1);
    callGetElementShouldFail(l3, 2);
    assertEquals(l3.get(0), Integer.valueOf(2));
    assertEquals(l3.get(1), Integer.valueOf(3));

    callGetElementShouldFail(l1l2, -1);
    callGetElementShouldFail(l1l2, 1);
    assertEquals(l1l2.get(0), Integer.valueOf(1));

    callGetElementShouldFail(l1l2l3, -1);
    callGetElementShouldFail(l1l2l3, 3);
    assertEquals(l1l2l3.get(0), Integer.valueOf(1));
    assertEquals(l1l2l3.get(1), Integer.valueOf(2));
    assertEquals(l1l2l3.get(2), Integer.valueOf(3));

    callGetElementShouldFail(l3l3l1l2, -1);
    callGetElementShouldFail(l3l3l1l2, 5);
    assertEquals(l3l3l1l2.get(0), Integer.valueOf(2));
    assertEquals(l3l3l1l2.get(1), Integer.valueOf(3));
    assertEquals(l3l3l1l2.get(2), Integer.valueOf(2));
    assertEquals(l3l3l1l2.get(3), Integer.valueOf(3));
    assertEquals(l3l3l1l2.get(4), Integer.valueOf(1));
  }
}
