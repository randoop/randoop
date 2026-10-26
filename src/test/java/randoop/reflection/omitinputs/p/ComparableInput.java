package randoop.reflection.omitinputs.p;

/** Input for testing omitmethod patterns: a class that implements a generic interface. */
public class ComparableInput implements Comparable<ComparableInput> {
  @Override
  public int compareTo(ComparableInput other) {
    return 0;
  }

  /**
   * An overload of {@code compareTo} that does not override {@code Comparable.compareTo}.
   *
   * @param other an integer
   * @return 0
   */
  public int compareTo(Integer other) {
    return 0;
  }
}
