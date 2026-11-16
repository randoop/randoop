package randoop.reflection;

/** Methods whose parameter type is a member type that declares no type parameters. */
public class MemberTypeCase {

  /**
   * Does nothing.
   *
   * @param <U> the type argument of the enclosing type of the parameter type
   * @param inner a {@code MemberTypeOuter<U>.Inner}
   */
  public static <U> void use(MemberTypeOuter<U>.Inner inner) {}

  /**
   * Does nothing.
   *
   * @param <U> the type argument of the enclosing type of the parameter type, which has a bound
   * @param inner a {@code MemberTypeOuter<U>.Inner}
   */
  public static <U extends Number> void useBounded(MemberTypeOuter<U>.Inner inner) {}

  /**
   * Throws an exception; only the signature is of interest.
   *
   * @param <U> the type argument of the enclosing type of the parameter type
   * @param inner a {@code MemberTypeOuter<U>.Inner}
   * @return never returns normally
   */
  public static <U> U get(MemberTypeOuter<U>.Inner inner) {
    throw new UnsupportedOperationException();
  }
}
