package randoop.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import junit.framework.TestCase;
import org.plumelib.util.SIList;
import randoop.util.Randomness;

public class ListOfListsIteratorTests extends TestCase {

  public void test() {

    SIList<Integer> a1 = SIList.fromList(Arrays.asList(1, 2));

    SIList<Integer> a2 = SIList.empty();

    SIList<Integer> a3 = SIList.fromList(Arrays.asList(3, 4, 5));

    for (int i = 0; i < 100; i++) {

      // Create random list.
      List<SIList<Integer>> lists = new ArrayList<>();
      lists.add(a1);
      lists.add(a2);
      lists.add(a3);
      int operations = Randomness.nextRandomInt(30);
      for (int j = 0; j < operations; j++) {
        int whichOperation = Randomness.nextRandomInt(2);
        if (whichOperation == 0) {
          // ListOfLists
          List<SIList<Integer>> members = new ArrayList<>();
          int howManyLists = Randomness.nextRandomInt(lists.size());
          for (int k = 0; k < howManyLists; k++) {
            members.add(Randomness.randomMember(lists));
          }
          SIList<Integer> theList = SIList.concat(members);
          lists.add(theList);
        } else {
          int theElement = Randomness.nextRandomInt(10);
          SIList<Integer> prefixList = Randomness.randomMember(lists);
          SIList<Integer> theList = prefixList.add(theElement);
          lists.add(theList);
        }
      }

      // Test iterator.
      for (SIList<Integer> l : lists) {
        // System.out.print("[ ");
        // for (int j = 0 ; j < l.size() ; j++)
        // System.out.print(l.get(j) + " ");
        // System.out.println("]");
        // System.out.print("[ ");
        Iterator<Integer> it = l.iterator();
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
