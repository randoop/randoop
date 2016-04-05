package randoop.test;

import java.util.ArrayList;
import java.util.List;

import randoop.util.ArrayListSimpleList;
import randoop.util.ListOfLists;
import randoop.util.SimpleList;

import junit.framework.Assert;
import junit.framework.TestCase;

public class ListOfListsSelectorTest extends TestCase {

  ListOfLists<Integer> empty;
  ListOfLists<Integer> l1;
  ListOfLists<Integer> l3;
  ListOfLists<Integer> l1l2;
  ListOfLists<Integer> l1l2l3;
  ListOfLists<Integer> l3l3l1l2;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    ArrayListSimpleList<Integer> l1List = new ArrayListSimpleList<>();
    ArrayListSimpleList<Integer> l2List = new ArrayListSimpleList<>();
    l2List.add(1);
    ArrayListSimpleList<Integer> l3List = new ArrayListSimpleList<>();
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

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
  }

  /*
   * Test method for 'randoop.util.ListOfLists.ListOfLists(List&lt;List&lt;T&gt;&gt;)'
   */
  public void testListOfLists() {}

  /*
   * Test method for 'randoop.util.ListOfLists.size()'
   */
  public void testSize() {
    Assert.assertEquals(empty.size(), 0);
    Assert.assertEquals(l1.size(), 0);
    Assert.assertEquals(l3.size(), 2);
    Assert.assertEquals(l1l2.size(), 1);
    Assert.assertEquals(l1l2l3.size(), 3);
    Assert.assertEquals(l3l3l1l2.size(), 5);
  }

  private static void callGetElementShouldFail(ListOfLists<?> s, int i) {
    try {
      s.get(i);
      fail("Should raise an IllegalArgumentException");
    } catch (IllegalArgumentException expected) {
      assertTrue(true);
    }
  }

  /*
   * Test method for 'randoop.util.ListOfLists.get(int)'
   */
  public void testGetElement() {

    callGetElementShouldFail(empty, 0);
    callGetElementShouldFail(empty, -1);
    callGetElementShouldFail(empty, 1);

    callGetElementShouldFail(l1, 0);
    callGetElementShouldFail(l1, -1);
    callGetElementShouldFail(l1, 1);

    callGetElementShouldFail(l3, -1);
    callGetElementShouldFail(l3, 2);
    Assert.assertEquals(l3.get(0), new Integer(2));
    Assert.assertEquals(l3.get(1), new Integer(3));

    callGetElementShouldFail(l1l2, -1);
    callGetElementShouldFail(l1l2, 1);
    Assert.assertEquals(l1l2.get(0), new Integer(1));

    callGetElementShouldFail(l1l2l3, -1);
    callGetElementShouldFail(l1l2l3, 3);
    Assert.assertEquals(l1l2l3.get(0), new Integer(1));
    Assert.assertEquals(l1l2l3.get(1), new Integer(2));
    Assert.assertEquals(l1l2l3.get(2), new Integer(3));

    callGetElementShouldFail(l3l3l1l2, -1);
    callGetElementShouldFail(l3l3l1l2, 5);
    Assert.assertEquals(l3l3l1l2.get(0), new Integer(2));
    Assert.assertEquals(l3l3l1l2.get(1), new Integer(3));
    Assert.assertEquals(l3l3l1l2.get(2), new Integer(2));
    Assert.assertEquals(l3l3l1l2.get(3), new Integer(3));
    Assert.assertEquals(l3l3l1l2.get(4), new Integer(1));
  }
}
