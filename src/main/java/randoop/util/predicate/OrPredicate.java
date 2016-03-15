package randoop.util.predicate;

/**
 * A Predicate that implements the or-else operator on two predicates. The
 * or-else operator is a short-circuited or, which only tests the second
 * predicate if the first returns false.
 *
 * @param <T>
 *          the type of object tested by the predicates
 */
public class OrPredicate<T> extends DefaultPredicate<T> {

  private Predicate<T> first;
  private Predicate<T> second;

  /**
   * Constructs a predicate that will test the first predicate and then the
   * second.
   *
   * @param first
   *          the predicate to test first
   * @param second
   *          the predicate to test second
   */
  public OrPredicate(Predicate<T> first, Predicate<T> second) {
    this.first = first;
    this.second = second;
  }

  /**
   * {@inheritDoc} Return the short-circuited or of the two predicates for the
   * value t
   *
   * @return true if either the first or second predicate is true on t, and
   *         false otherwise
   */
  @Override
  public boolean test(T t) {
    return first.test(t) || second.test(t);
  }

  @Override
  public String toString() {
    return "OR[" + first + ", " + second.toString() + "]";
  }
}
