package randoop.reflection;

/**
 * A generic class with a member class that declares no type parameters.
 *
 * @param <T> a type parameter
 */
public class MemberTypeOuter<T> {

  /** A member class that is generic only because its enclosing class is. */
  public class Inner {}
}
