package randoop.util;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;

/**
 * Test (mainly) to make sure iterators over simple lists work
 * 
 * @author bjkeller
 *
 */
public class SimpleListTest {

  @Test
  public void testArrayList() {
    ArrayList<String> al = new ArrayList<>();
    for (int i = 0; i < 100; i++) al.add("str" + i);
    
    SimpleList<String> sl = new ArrayListSimpleList<>(al);
    
    Iterator<String> it = sl.iterator();
    while (it.hasNext()) {
      assertTrue("element should be in original",al.contains(it.next()));
    }
  }
  
  @Test
  public void oneMoreElement() {
    ArrayList<String> al = new ArrayList<>();
    for (int i = 0; i < 100; i++) al.add("str" + i);
    
    SimpleList<String> sl = new OneMoreElementList<>(new ArrayListSimpleList<>(al), "str" + 100);
    
    al.add("str" + 100);
    
    Iterator<String> it = sl.iterator();
    while (it.hasNext()) {
      assertTrue("element should be in original",al.contains(it.next()));
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
      sum += (int)(Math.random()*47);
      partitions.add(sum);
    }
    
    for (int i = 0; i < 100; i++) {
      if (!sub.isEmpty() && partitions.contains(i) ) {
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
    
    SimpleList<String> sl = new ListOfLists<String>(lists);
    
    Iterator<String> it = sl.iterator();
    while (it.hasNext()) {
      assertTrue("element should be in original",al.contains(it.next()));
    }
  }
}
