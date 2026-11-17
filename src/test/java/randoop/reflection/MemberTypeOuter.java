package randoop.reflection;

/**
 * A generic class with a member class that declares no type parameters.
 *
 * @param <T> a type parameter
 */
public class MemberTypeOuter<T> {

  /** A member class that is generic only because its enclosing class is. */
  public class Inner {}

  /** A member class that declares no type parameters and that has a member class. */
  public class Middle {

    /** A member class whose enclosing types are a non-generic and a generic class. */
    public class Leaf {}
  }

  /**
   * A member class that declares a type parameter.
   *
   * @param <V> a type parameter
   */
  public class Inner2<V> {}
}
