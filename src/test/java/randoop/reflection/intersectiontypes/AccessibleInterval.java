package randoop.reflection.intersectiontypes;

/** Created by bjkeller on 12/6/16. */
public class AccessibleInterval implements Interval, RandomAccessible<Integer> {
  @Override
  public int n() {
    return 0;
  }

  @Override
  public void dimensions(long[] dimensions) {}

  @Override
  public double min(int d) {
    return 0;
  }

  @Override
  public RandomAccess<Integer> randomAccess() {
    return new RandomAccess<Integer>() {
      @Override
      public RandomAccess<Integer> copy() {
        return null;
      }
    };
  }
}
