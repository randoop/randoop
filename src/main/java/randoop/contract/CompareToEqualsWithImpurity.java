package randoop.contract;

/**
 * The contract: Checks that compareTo is consistent with equals, except for StringBuilder. This
 * contract is a special case of CompareToEquals to handle "GRT Impurity", which uses StringBuilder
 * to fuzz String inputs. However, StringBuilder does not override the equals method even though it
 * implements the Comparable interface. Hence, we need to ignore StringBuilder while checking
 * compareToEquals contract.
 *
 * <pre>(x0.compareTo(x1) == 0) == x0.equals(x1)</pre>
 */
public class CompareToEqualsWithImpurity extends CompareToEquals {
  /** The singleton instance of this class. */
  private static final CompareToEqualsWithImpurity instance = new CompareToEqualsWithImpurity();

  /**
   * Creates the singleton CompareToEqualsWithImpurity; is only ever called once. Clients should
   * call {@link #getInstance}.
   */
  private CompareToEqualsWithImpurity() {}

  /** Returns the singleton instance of this class. */
  public static CompareToEqualsWithImpurity getInstance() {
    return instance;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public boolean evaluate(Object... objects) {
    Object o1 = objects[0];
    Object o2 = objects[1];

    // Ignore StringBuilder
    if (o1 instanceof Comparable && !(o1 instanceof StringBuilder)) {
      Comparable compObj1 = (Comparable) o1;
      return (compObj1.compareTo(o2) == 0) == o1.equals(o2);
    }
    return true;
  }

  @Override
  public String get_observer_str() {
    return "CompareToEqualsWithImpurity";
  }
}
