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

    abstract Sub<Integer> subType();

    abstract List<Integer> integerList();

    abstract List<? extends Outer<String>.Inner> innerUpperWildcardList();

    abstract List<? super Outer<String>.Inner> innerLowerWildcardList();

    abstract Outer<?>.Inner wildcardInnerType();

    abstract Outer<? extends CharSequence>.Inner charSequenceInnerType();

    abstract Outer<Long>.Inner longInnerType();
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
   * Returns the type {@code Outer<String>.C} for the given member class {@code C} of {@link Outer}.
   * The type is constructed by substitution, because {@link Type#forType} discards the type
   * arguments of the enclosing type of a non-generic member class, yielding {@code Outer<T>.C}.
   *
   * @param memberClass a non-generic member class of {@link Outer}
   * @return the member class, with {@code Outer<String>} as its enclosing type
   */
  private static ClassOrInterfaceType memberOfOuterOfString(Class<?> memberClass) {
    ClassOrInterfaceType genericMember = ClassOrInterfaceType.forClass(memberClass);
    return genericMember.substitute(
        new Substitution(genericMember.getTypeParameters(), JavaTypes.STRING_TYPE));
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

  @Test
  public void testSubclassOfMemberClassWithWildcardEnclosingType() throws NoSuchMethodException {
    ReferenceType inner = returnType("innerType");
    ReferenceType sub = returnType("subType");
    ReferenceType wildcardInner = returnType("wildcardInnerType");
    ReferenceType charSequenceInner = returnType("charSequenceInnerType");
    ReferenceType longInner = returnType("longInnerType");

    assertTrue(inner.isSubtypeOfOrEqualTo(wildcardInner));
    assertTrue(sub.isSubtypeOfOrEqualTo(inner));
    assertTrue(sub.isSubtypeOfOrEqualTo(wildcardInner));
    assertTrue(sub.isSubtypeOfOrEqualTo(charSequenceInner));
    assertFalse(sub.isSubtypeOfOrEqualTo(longInner));
    assertFalse(inner.isSubtypeOfOrEqualTo(longInner));

    // Raw Sub is assignable to Outer<?>.Inner only by unchecked conversion, not by subtyping.
    ReferenceType rawSub = NonParameterizedType.forClass(Sub.class);
    assertTrue(rawSub.isRawtype());
    assertFalse(rawSub.isSubtypeOfOrEqualTo(wildcardInner));
  }

  @Test
  public void testSubstitutedMemberClassOfParameterizedType() throws NoSuchMethodException {
    ClassOrInterfaceType inner = memberOfOuterOfString(Outer.Inner.class);
    assertTrue(inner.isParameterized());
    assertFalse(inner instanceof InstantiatedType);

    ReferenceType integerList = returnType("integerList");
    Substitution empty = new Substitution();

    assertFalse(new EagerReferenceBound(integerList).isLowerBound(inner, empty));
    assertFalse(new EagerReferenceBound(inner).isUpperBound(integerList, empty));
    assertFalse(integerList.isSubtypeOfOrEqualTo(inner));
  }
}
