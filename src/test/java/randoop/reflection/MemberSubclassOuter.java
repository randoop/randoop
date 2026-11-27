package randoop.reflection;

/**
 * A generic class whose member classes are extended by classes that are not generic.
 *
 * @param <T> a type parameter
 */
public class MemberSubclassOuter<T> {

  /** A member class with no type parameters of its own. */
  public class Inner {
    /**
     * Returns null.
     *
     * @return null
     */
    public T getT() {
      return null;
    }
  }

  /**
   * A member class that declares a type parameter.
   *
   * @param <I> a type parameter
   */
  public class GenericInner<I> {
    /**
     * Returns null.
     *
     * @return null
     */
    public T getT() {
      return null;
    }

    /**
     * Returns null.
     *
     * @return null
     */
    public I getI() {
      return null;
    }
  }

  /** A member class whose superclass is a generic member class of the same class. */
  public class StringInner extends GenericInner<String> {}

  /** A subclass of {@code MemberSubclassOuter<Integer>.Inner}. */
  public static class InnerSubclass extends MemberSubclassOuter<Integer>.Inner {
    /**
     * Creates an InnerSubclass.
     *
     * @param outer the enclosing instance of the superclass
     */
    public InnerSubclass(MemberSubclassOuter<Integer> outer) {
      outer.super();
    }
  }

  /** A subclass of {@code MemberSubclassOuter<Integer>.StringInner}. */
  public static class StringInnerSubclass extends MemberSubclassOuter<Integer>.StringInner {
    /**
     * Creates a StringInnerSubclass.
     *
     * @param outer the enclosing instance of the superclass
     */
    public StringInnerSubclass(MemberSubclassOuter<Integer> outer) {
      outer.super();
    }
  }
}
