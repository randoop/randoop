package generror;

import java.util.Comparator;

/**
 * Structure borrowed from Guava com.google.common.primitives
 */
public final class Ints {

  public static Comparator<int[]> lexicographicalComparator() {
    return LexicographicalComparator.INSTANCE;
  }

  private enum LexicographicalComparator implements Comparator<int[]> {
    INSTANCE;

    @Override
    public int compare(int[] left, int[] right) {
      int minLength = Math.min(left.length, right.length);
      for (int i = 0; i < minLength; i++) {
        int result = Ints.compare(left[i], right[i]);
        if (result != 0) {
          return result;
        }
      }
      return left.length - right.length;
    }

    @Override
    public String toString() {
      return "Ints.lexicographicalComparator()";
    }
  }

  public static int compare(int a, int b) {
    return (a < b) ? -1 : ((a > b) ? 1 : 0);
  }
}
