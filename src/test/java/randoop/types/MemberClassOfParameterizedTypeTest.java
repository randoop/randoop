package randoop.types;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Test;

/**
 * Tests a non-generic member class of a parameterized type, such as {@code Outer<String>.Inner}.
 * Such a type is parameterized (through its enclosing type), but it is a {@link
 * NonParameterizedType}, not an {@link InstantiatedType}.
 */
public class MemberClassOfParameterizedTypeTest {

  /**
   * A generic class with non-generic member classes.
   *
   * @param <T> the type parameter
   */
  static class Outer<T> {
    /** A non-generic member class of a generic class. */
    class Inner {}

    /** Another non-generic member class of a generic class. */
    class Other {}

    /** A non-generic member class that extends another member class of the same generic class. */
    class SubInner extends Inner {}

    /** A non-generic member class whose interface uses the type parameter of the generic class. */
    @SuppressWarnings("ComparableType")
    abstract class ComparableInner implements Comparable<T> {}
  }

  /**
   * A generic subclass of a member class of a parameterized type.
   *
   * @param <U> the type parameter
   */
  static class Sub<U> extends Outer<String>.Inner {
    /**
     * Creates a Sub.
     *
     * @param outer an enclosing instance for the superclass
     */
    Sub(Outer<String> outer) {
      outer.super();
    }
  }

  /** Methods whose return types are used in the tests. */
  @SuppressWarnings("unused")
  abstract static class TypeHolder {
    abstract Outer<String>.Inner innerType();

    abstract Outer<String>.Other otherType();

    abstract Outer<Integer>.Inner integerInnerType();

    abstract Outer<String>.SubInner subInnerType();

    abstract Outer<String>.ComparableInner comparableInnerType();

    abstract Comparable<String> stringComparable();

    abstract Comparable<Integer> integerComparable();

    abstract Sub<Integer> subType();

    abstract List<Integer> integerList();

    abstract List<? extends Outer<String>.Inner> innerUpperWildcardList();

    abstract List<? super Outer<String>.Inner> innerLowerWildcardList();
  }

  /**
   * Returns the Randoop type for the return type of the given method of {@link TypeHolder}.
   *
   * @param methodName a method name of {@link TypeHolder}, such as {@code "innerType"}
   * @return the return type of the method
   * @throws NoSuchMethodException if {@link TypeHolder} has no method named {@code methodName}
   */
  private static ReferenceType returnType(String methodName) throws NoSuchMethodException {
    return (ReferenceType)
        Type.forType(TypeHolder.class.getDeclaredMethod(methodName).getGenericReturnType());
  }

  /**
   * Tests that the supertypes of a member class of a parameterized type, such as {@code
   * Outer<String>.SubInner}, have the type arguments of its enclosing type.
   */
  @Test
  public void testSupertypesOfMemberClassOfParameterizedType() throws NoSuchMethodException {
    ReferenceType subInner = returnType("subInnerType");
    assertTrue(subInner.isSubtypeOfOrEqualTo(returnType("innerType")));
    assertFalse(subInner.isSubtypeOfOrEqualTo(returnType("integerInnerType")));

    ReferenceType comparableInner = returnType("comparableInnerType");
    assertTrue(comparableInner.isSubtypeOfOrEqualTo(returnType("stringComparable")));
    assertFalse(comparableInner.isSubtypeOfOrEqualTo(returnType("integerComparable")));
  }

  @Test
  public void testEagerBoundOfMemberClassOfParameterizedType() throws NoSuchMethodException {
    ReferenceType inner = returnType("innerType");
    ReferenceType sub = returnType("subType");
    Substitution empty = new Substitution();

    EagerReferenceBound innerBound = new EagerReferenceBound(inner);
    assertTrue(innerBound.isUpperBound(sub, empty));
    assertTrue(innerBound.isUpperBound(inner, empty));
    assertFalse(innerBound.isUpperBound(returnType("integerList"), empty));

    EagerReferenceBound subBound = new EagerReferenceBound(sub);
    assertTrue(subBound.isLowerBound(inner, empty));
    assertFalse(subBound.isLowerBound(returnType("otherType"), empty));
  }

  /**
   * Returns the type argument of the return type of the given method of {@link TypeHolder}.
   *
   * @param methodName the method name
   * @return the type argument of the return type of the method
   * @throws NoSuchMethodException if {@link TypeHolder} has no method named {@code methodName}
   */
  private static TypeArgument typeArgument(String methodName) throws NoSuchMethodException {
    return ((InstantiatedType) returnType(methodName)).getTypeArguments().get(0);
  }

  @Test
  public void testWildcardBoundedByMemberClassOfParameterizedType() throws NoSuchMethodException {
    TypeArgument inner = TypeArgument.forType(returnType("innerType"));
    TypeArgument sub = TypeArgument.forType(returnType("subType"));
    TypeArgument other = TypeArgument.forType(returnType("otherType"));

    TypeArgument upper = typeArgument("innerUpperWildcardList");
    assertTrue(upper.contains(inner));
    assertTrue(upper.contains(sub));
    assertFalse(upper.contains(other));

    TypeArgument lower = typeArgument("innerLowerWildcardList");
    assertTrue(lower.contains(inner));
    assertFalse(lower.contains(sub));
    assertFalse(lower.contains(other));
  }
}
