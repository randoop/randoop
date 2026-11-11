package randoop.reflection;

/** A method whose parameter type is a member type that declares no type parameters. */
public class MemberTypeCase {

  /**
   * Does nothing.
   *
   * @param <U> the type argument of the enclosing type of the parameter type
   * @param inner a {@code MemberTypeOuter<U>.Inner}
   */
  public static <U> void use(MemberTypeOuter<U>.Inner inner) {}
}
