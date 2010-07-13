package randoop.util;

import randoop.Globals;

/**
 * Compares two SimpleLists for member-wise equality, e.g.
 * two lists l1 and l2 are equals if they have the same size and at every
 * valid index i, l1.get(i).equals(l2.get(i))==true.
 * 
 * The compare method returns a ComparisonResult, which in addition
 * to returning the boolean result of the comparison, also returns
 * a message explaining the result, such as the first index at which the
 * collections differ, the toString() representation of the first
 * differing elements, etc.
 */
public class InformationalComparator<T> {

  /** Communicates the result of the comparison,
   * and  also a message describing the result. */
  public static class ComparisonResult {

    public String message;
    public boolean listsAreEqual;

    public ComparisonResult(boolean listsAreEqual, String message) {
      this.message = message;
      this.listsAreEqual = listsAreEqual;
    }
  }

  /** Compares two lists for equality. */
  public ComparisonResult compare(String l1Name, SimpleList<T> l1, String l2Name, SimpleList<T> l2) {

    StringBuilder b = new StringBuilder();

    boolean comparisonSuccess = true;

    int lastIndex = 0;

    if (equalSize(l1, l2)) {
      lastIndex = l1.size();
    } else {
      lastIndex = sizeOfSmaller(l1, l2);
      comparisonSuccess = false;
      b.append("Lists differ in size. List " + l1Name + " has size " +
          l1.size() + " and list " + l2Name + " has size " + l2.size() + Globals.lineSep);
    }

    for (int i = 0 ; i < lastIndex ; i++) {

      if (i < l1.size() && i >= l2.size()) {
        assert !comparisonSuccess;
        b.append("Smaller suite is equal to larger suite up to its last index.");
        break;
      }

      if (i < l2.size() && i >= l1.size()) {
        assert !comparisonSuccess;
        b.append("Smaller suite is equal to larger suite up to its last index.");                
        break;
      }

      // At this point, we know i is a valid index
      // into both lists. Compare elements at i.
      if (!Util.equalsWithNull(l1.get(i), l2.get(i))) {
        comparisonSuccess = false;
        b.append("Lists differ at index " + i 
            + "." + Globals.lineSep);
        b.append(l1Name + " element at this index:" + Globals.lineSep);
        b.append(l1.get(i) + Globals.lineSep);
        b.append(l2Name + " element at this index:" + Globals.lineSep);
        b.append(l2.get(i) + Globals.lineSep);
        break;
      }
    }

    return new ComparisonResult(comparisonSuccess, b.toString());
  }

  private boolean equalSize(SimpleList<T> l1, SimpleList<T> l2) {
    return l1.size() == l2.size();
  }

  private int sizeOfSmaller(SimpleList<T> l1, SimpleList<T> l2) {
    if (equalSize(l1, l2))
      throw new IllegalArgumentException("Lists have equal size");
    if (l1.size() < l2.size())
      return l1.size();
    else
      return l2.size();
  }
}
