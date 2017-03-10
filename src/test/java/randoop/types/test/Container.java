package randoop.types.test;

import java.util.TreeSet;

/** Input class for capture conversion used in {@link randoop.types.CaptureConversionTest}. */
public class Container<T extends Comparable<T>> {
  private TreeSet<T> set;
}
