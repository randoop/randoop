package randoop.util.predicate;

/**
 * Predicate that implements the and-also operator on a pair of predicates. The and-also operator is
 * a short-circuited and operator, which only tests the second predicate if the first predicate is
 * true.
 *
 * @param <T> the type of object to be tested
 */
public class AndPredicate<T> extends DefaultPredicate<T> {

  private Predicate<T> first;
  private Predicate<T> second;

  /**
   * Creates a Predicate that performs the and-also operator: testing the first predicate, and then
   * the second.
   *
   * @param first the predicate to test first
   * @param second the predicate to test second
   */
  public AndPredicate(Predicate<T> first, Predicate<T> second) {
    this.first = first;
    this.second = second;
  }

  /**
   * {@inheritDoc} Return the short-circuited and of the two predicates for the value t
   *
   * @return true if both the first and second predicate is true on t, and false otherwise
   */
  @Override
  public boolean test(T t) {
    return first.test(t) && second.test(t);
  }

  @Override
  public String toString() {
    return "AND[" + first + ", " + second + "]";
  }
}
