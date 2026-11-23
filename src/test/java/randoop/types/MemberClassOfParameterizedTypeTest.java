package randoop.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
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

    /**
     * A generic member class of a generic class.
     *
     * @param <V> the type parameter
     */
    class GenericInner<V> {}
  }

  /**
   * A class whose type parameter's bound is a generic member class whose owner type and type
   * argument contain the type parameter.
   *
   * @param <T> the type parameter
   */
  static class GenericMemberBound<T extends Outer<T>.GenericInner<T>> {}

  /**
   * A class whose type parameter's bound is a generic member class whose owner type, but not its
   * type argument, contains the type parameter.
   *
   * @param <T> the type parameter
   */
  static class OwnerOnlyBound<T extends Outer<T>.GenericInner<String>> {}

  /**
   * A generic subclass of a member class of a parameterized type.
   *
   * @param <U> the type parameter
   */
  static class Sub<U> extends Outer<String>.Inner {
    /**
     * Creates a Sub.
     *
     * @param outer the enclosing instance of the superclass
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

    abstract Sub<Integer> subType();

    abstract List<Integer> integerList();

    abstract Outer<Integer>.Inner integerInnerType();

    abstract Outer<?>.Inner wildcardInnerType();

    abstract Outer<? extends Number>.Inner numberInnerType();

    abstract Outer<String>.GenericInner<String> stringGenericInnerType();

    abstract List<? extends Outer<String>.Inner> innerUpperWildcardList();

    abstract List<? super Outer<String>.Inner> innerLowerWildcardList();
  }

  /**
   * Returns the Randoop type for the return type of the given method of {@link TypeHolder}.
   *
   * @param methodName the method name
   * @return the return type of the method
   * @throws NoSuchMethodException if {@link TypeHolder} has no method named {@code methodName}
   */
  private static ReferenceType returnType(String methodName) throws NoSuchMethodException {
    return (ReferenceType)
        Type.forType(TypeHolder.class.getDeclaredMethod(methodName).getGenericReturnType());
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

  @Test
  public void testSubtypeOfMemberClassWithWildcardOwner() throws NoSuchMethodException {
    ReferenceType sub = returnType("subType");
    // Sub<Integer> extends Outer<String>.Inner, and Outer<String> is a subtype of Outer<?>.
    assertTrue(sub.isSubtypeOfOrEqualTo(returnType("wildcardInnerType")));
    assertTrue(sub.isSubtypeOfOrEqualTo(returnType("innerType")));
    assertFalse(sub.isSubtypeOfOrEqualTo(returnType("integerInnerType")));
    assertFalse(sub.isSubtypeOfOrEqualTo(returnType("numberInnerType")));

    ReferenceType integerInner = returnType("integerInnerType");
    assertTrue(integerInner.isSubtypeOfOrEqualTo(returnType("numberInnerType")));
    assertTrue(integerInner.isSubtypeOfOrEqualTo(returnType("wildcardInnerType")));
    assertFalse(integerInner.isSubtypeOfOrEqualTo(returnType("innerType")));
    assertFalse(returnType("innerType").isSubtypeOfOrEqualTo(returnType("numberInnerType")));
  }

  @Test
  public void testEagerBoundOfMemberClassWithWildcardOwner() throws NoSuchMethodException {
    Substitution empty = new Substitution();
    EagerReferenceBound numberInnerBound = new EagerReferenceBound(returnType("numberInnerType"));
    assertTrue(numberInnerBound.isUpperBound(returnType("integerInnerType"), empty));
    assertFalse(numberInnerBound.isUpperBound(returnType("innerType"), empty));
    assertFalse(numberInnerBound.isUpperBound(returnType("subType"), empty));
  }

  @Test
  public void testLazyBoundOfGenericMemberClassKeepsOwner() throws NoSuchMethodException {
    TypeVariable t = GenericClassType.forClass(GenericMemberBound.class).getTypeParameters().get(0);
    ParameterBound bound = t.getUpperTypeBound();
    assertTrue(bound instanceof LazyParameterBound);
    ParameterBound substituted = bound.substitute(new Substitution(t, JavaTypes.STRING_TYPE));
    assertEquals(
        returnType("stringGenericInnerType"), ((ReferenceBound) substituted).getBoundType());
  }

  @Test
  public void testBoundWithTypeVariableOnlyInOwnerIsLazy() throws NoSuchMethodException {
    TypeVariable t = GenericClassType.forClass(OwnerOnlyBound.class).getTypeParameters().get(0);
    ParameterBound bound = t.getUpperTypeBound();
    assertTrue(bound instanceof LazyParameterBound);
    assertEquals(Collections.singletonList(t), bound.getTypeParameters());
    ParameterBound substituted = bound.substitute(new Substitution(t, JavaTypes.STRING_TYPE));
    assertEquals(
        returnType("stringGenericInnerType"), ((ReferenceBound) substituted).getBoundType());
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
