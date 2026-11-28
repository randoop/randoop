package randoop.generation;

/**
 * A class under test whose methods use a member class of a parameterized type, {@code
 * Outer<String>.Inner}. Such a type is parameterized (through its enclosing type), but it is a
 * {@link randoop.types.NonParameterizedType}, not a {@link randoop.types.ParameterizedType}.
 */
public class MemberClassInputs {

  /** Creates a MemberClassInputs. */
  public MemberClassInputs() {}

  /**
   * A generic class with a non-generic member class.
   *
   * @param <T> the type parameter
   */
  public static class Outer<T> {
    /** Creates an Outer. */
    public Outer() {}

    /** A non-generic member class of a generic class. */
    public class Inner {
      /** Creates an Inner. */
      public Inner() {}
    }
  }

  /**
   * Returns a new {@code Outer<String>.Inner}.
   *
   * @return a new {@code Outer<String>.Inner}
   */
  public static Outer<String>.Inner makeInner() {
    return new Outer<String>().new Inner();
  }

  /**
   * Returns 1.
   *
   * @param inner an {@code Outer<String>.Inner}, which is ignored
   * @return 1
   */
  public static int consumeInner(Outer<String>.Inner inner) {
    return 1;
  }

  /**
   * Returns the length of the given array.
   *
   * @param inners an array of {@code Outer<String>.Inner}
   * @return the length of {@code inners}
   */
  public static int consumeInnerArray(Outer<String>.Inner[] inners) {
    return inners.length;
  }
}
