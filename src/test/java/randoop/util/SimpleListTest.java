package randoop.util;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;

/**
 * Test (mainly) to make sure iterators over simple lists work.
 *
 */
public class SimpleListTest {

  @Test
  public void testArrayList() {
    ArrayList<String> al = new ArrayList<>();
    for (int i = 0; i < 100; i++) al.add("str" + i);

    SimpleList<String> sl = new ArrayListSimpleList<>(al);

    for (int i = 0; i < sl.size(); i++) {
      assertTrue("element should be in original", al.contains(sl.get(i)));
    }
  }

  @Test
  public void oneMoreElement() {
    ArrayList<String> al = new ArrayList<>();
    for (int i = 0; i < 100; i++) al.add("str" + i);

    SimpleList<String> sl = new OneMoreElementList<>(new ArrayListSimpleList<>(al), "str" + 100);

    al.add("str" + 100);

    for (int i = 0; i < sl.size(); i++) {
      assertTrue("element should be in original", al.contains(sl.get(i)));
    }
  }

  @Test
  public void listOfList() {
    ArrayList<String> al = new ArrayList<>();
    ArrayList<String> sub = new ArrayList<>();
    List<SimpleList<String>> lists = new ArrayList<>();

    Set<Integer> partitions = new TreeSet<>();
    int sum = 0;
    while (sum < 100) {
      sum += (int) (Math.random() * 47);
      partitions.add(sum);
    }

    for (int i = 0; i < 100; i++) {
      if (partitions.contains(i)) {
        lists.add(new ArrayListSimpleList<>(sub));
        sub = new ArrayList<>();
      }
      String str = "str" + i;
      al.add(str);
      sub.add(str);
    }

    if (!sub.isEmpty()) {
      lists.add(new ArrayListSimpleList<>(sub));
    }

    SimpleList<String> sl = new ListOfLists<>(lists);

    for (int i = 0; i < sl.size(); i++) {
      assertTrue("element should be in original", al.contains(sl.get(i)));
    }
  }

  @Test
  public void listOfMixed() {

    List<SimpleList<String>> lists = new ArrayList<>();
    ArrayList<String> al = new ArrayList<>();

    SimpleList<String> base = new ArrayListSimpleList<>(new ArrayList<String>());

    int i;
    for (i = 0; i < 50; i++) {
      String v = "str" + i;
      base = new OneMoreElementList<>(base, v);
      al.add(v);
    }
    lists.add(base);
    lists.add(new ArrayListSimpleList<>(new ArrayList<String>()));
    base = new ListOfLists<>(lists);
    for (i = 55; i < 70; i++) {
      String v = "str" + i;
      base = new OneMoreElementList<>(base, v);
      al.add(v);
    }

    for (int j = 0; i < base.size(); j++) {
      assertTrue("element should be in original", al.contains(base.get(j)));
    }
  }

  @Test
  public void emptyLOL() {
    List<SimpleList<String>> lists = new ArrayList<>();
    lists.add(new ArrayListSimpleList<>(new ArrayList<String>()));
    SimpleList<String> sl = new ListOfLists<>(lists);

    assertTrue("should be no elements", sl.isEmpty());
  }
}
