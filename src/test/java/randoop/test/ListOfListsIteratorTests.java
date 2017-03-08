package randoop.test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import junit.framework.TestCase;
import randoop.util.ArrayListSimpleList;
import randoop.util.ListOfLists;
import randoop.util.OneMoreElementList;
import randoop.util.Randomness;
import randoop.util.SimpleList;

public class ListOfListsIteratorTests extends TestCase {

  public void test() {

    ArrayListSimpleList<Integer> a1 = new ArrayListSimpleList<>();
    a1.add(1);
    a1.add(2);

    ArrayListSimpleList<Integer> a2 = new ArrayListSimpleList<>();

    ArrayListSimpleList<Integer> a3 = new ArrayListSimpleList<>();
    a3.add(3);
    a3.add(4);
    a3.add(5);

    List<SimpleList<Integer>> level0 = new ArrayList<>();
    level0.add(a1);
    level0.add(a2);
    level0.add(a3);

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
          SimpleList<Integer> theList = new ListOfLists<>(members);
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
        //System.out.print("[ ");
        //for (int j = 0 ; j < l.size() ; j++)
        //System.out.print(l.get(j) + " ");
        //System.out.println("]");
        //System.out.print("[ ");
        Iterator<Integer> it = l.toJDKList().iterator();
        for (int j = 0; j < l.size(); j++) {
          //System.out.print(l.get(j) + " ");
          assertTrue(it.hasNext());
          Integer iteratorElt = it.next();
          assertEquals(l.get(j), iteratorElt);
        }
        //System.out.println("]");
        assertTrue(!it.hasNext());
      }
    }
  }
}
