package randoop.test;

import junit.framework.TestCase;
import randoop.util.ArrayListSimpleList;
import randoop.util.InformationalComparator;
import randoop.util.Util;
import randoop.util.InformationalComparator.ComparisonResult;

public class InformationalComparatorTests extends TestCase {

  public void test() {

    ArrayListSimpleList<Integer> l123 = new ArrayListSimpleList<Integer>();
    l123.add(1);l123.add(2);l123.add(3);

    ArrayListSimpleList<Integer> l143 = new ArrayListSimpleList<Integer>();
    l143.add(1);l143.add(4);l143.add(3);

    ArrayListSimpleList<Integer> l423 = new ArrayListSimpleList<Integer>();
    l423.add(4);l423.add(2);l423.add(3);

    ArrayListSimpleList<Integer> l124 = new ArrayListSimpleList<Integer>();
    l124.add(1);l124.add(2);l124.add(4);

    ArrayListSimpleList<Integer> l12 = new ArrayListSimpleList<Integer>();
    l12.add(1);l12.add(2);

    ArrayListSimpleList<Integer> lempty = new ArrayListSimpleList<Integer>();

    InformationalComparator<Integer> c = new InformationalComparator<Integer>();

    ComparisonResult r = null;

    r = c.compare("l123", l123, "l123", l123);
    assertTrue(r.listsAreEqual);
    assertEquals(r.message, "");

    r = c.compare("l12", l12, "l12", l12);
    assertTrue(r.listsAreEqual);
    assertEquals(r.message, "");

    r = c.compare("lempty", lempty, "lempty", lempty);
    assertTrue(r.listsAreEqual);
    assertEquals(r.message, "");

    r = c.compare("l123", l123, "l143", l143);
    assertFalse(r.listsAreEqual);
    assertEquals("Lists differ at index 1. l123 element at this index: 2 l143 element at this index: 4 ",
        r.message.replace(Util.newLine, " "));

    r = c.compare("l143", l143, "l123", l123);
    assertFalse(r.listsAreEqual);
    assertEquals("Lists differ at index 1. l143 element at this index: 4 l123 element at this index: 2 ",
        r.message.replace(Util.newLine, " "));

    r = c.compare("l123", l123, "l1423", l423);
    assertFalse(r.listsAreEqual);
    assertEquals("Lists differ at index 0. l123 element at this index: 1 l1423 element at this index: 4 ",
        r.message.replace(Util.newLine, " "));


    r = c.compare("l1423", l423, "l123", l123);
    assertFalse(r.listsAreEqual);
    assertEquals("Lists differ at index 0. l1423 element at this index: 4 l123 element at this index: 1 ",
        r.message.replace(Util.newLine, " "));



    r = c.compare("l123", l123, "l124", l124);
    assertFalse(r.listsAreEqual);
    assertEquals("Lists differ at index 2. l123 element at this index: 3 l124 element at this index: 4 ",
        r.message.replace(Util.newLine, " "));


    r = c.compare("l124", l124, "l123", l123);
    assertFalse(r.listsAreEqual);
    assertEquals("Lists differ at index 2. l124 element at this index: 4 l123 element at this index: 3 ",
        r.message.replace(Util.newLine, " "));


    r = c.compare("l123", l123, "l12", l12);
    assertFalse(r.listsAreEqual);
    assertEquals("Lists differ in size. List l123 has size 3 and list l12 has size 2 ",
        r.message.replace(Util.newLine, " "));


    r = c.compare("l12", l12, "l123", l123);
    assertFalse(r.listsAreEqual);
    assertEquals("Lists differ in size. List l12 has size 2 and list l123 has size 3 ",
        r.message.replace(Util.newLine, " "));


    r = c.compare("l123", l123, "lempty", lempty);
    assertFalse(r.listsAreEqual);
    assertEquals("Lists differ in size. List l123 has size 3 and list lempty has size 0 ",
        r.message.replace(Util.newLine, " "));


    r = c.compare("lempty", lempty, "l123", l123);
    assertFalse(r.listsAreEqual);
    assertEquals("Lists differ in size. List lempty has size 0 and list l123 has size 3 ",
        r.message.replace(Util.newLine, " "));

  }

}
