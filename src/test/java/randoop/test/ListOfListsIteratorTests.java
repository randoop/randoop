package randoop.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import junit.framework.TestCase;
import randoop.util.Randomness;
import randoop.util.list.OneMoreElementList;
import randoop.util.list.SimpleList;

public class ListOfListsIteratorTests extends TestCase {

  public void test() {

    SimpleList<Integer> a1 = SimpleList.fromList(Arrays.asList(1, 2));

    SimpleList<Integer> a2 = SimpleList.empty();

    SimpleList<Integer> a3 = SimpleList.fromList(Arrays.asList(3, 4, 5));

    for (int i = 0; i < 100; i++) {

      // Create random list.
      List<SimpleList<Integer>> lists = new ArrayList<>();
      lists.add(a1);
      lists.add(a2);
      lists.add(a3);
      int operations = Randomness.nextRandomInt(30);
      for (int j = 0; j < operations; j++) {
        int whichOperation = Randomness.nextRandomInt(2);
        if (whichOperation == 0) {
          // ListOfLists
          List<SimpleList<Integer>> members = new ArrayList<>();
          int howManyLists = Randomness.nextRandomInt(lists.size());
          for (int k = 0; k < howManyLists; k++) {
            members.add(Randomness.randomMember(lists));
          }
          SimpleList<Integer> theList = SimpleList.concat(members);
          lists.add(theList);
        } else {
          // OneMoreElementList
          int theElement = Randomness.nextRandomInt(10);
          SimpleList<Integer> prefixList = Randomness.randomMember(lists);
          SimpleList<Integer> theList = new OneMoreElementList<>(prefixList, theElement);
          lists.add(theList);
        }
      }

      // Test iterator.
      for (SimpleList<Integer> l : lists) {
        // System.out.print("[ ");
        // for (int j = 0 ; j < l.size() ; j++)
        // System.out.print(l.get(j) + " ");
        // System.out.println("]");
        // System.out.print("[ ");
        Iterator<Integer> it = l.toJDKList().iterator();
        for (int j = 0; j < l.size(); j++) {
          // System.out.print(l.get(j) + " ");
          assertTrue(it.hasNext());
          Integer iteratorElt = it.next();
          assertEquals(l.get(j), iteratorElt);
        }
        // System.out.println("]");
        assertFalse(it.hasNext());
      }
    }
  }
}
