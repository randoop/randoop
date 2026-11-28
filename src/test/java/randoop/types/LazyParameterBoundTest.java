package randoop.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.Test;

/** Tests {@link LazyParameterBound}. */
public class LazyParameterBoundTest {

  /**
   * A type parameter whose bound contains the type parameter and, nested in a type argument,
   * another type parameter.
   *
   * @param <U> the type parameter that occurs nested in the bound
   * @param <T> the type parameter with the bound
   */
  static class OtherNestedArgumentBound<U, T extends Map<List<U>, T>> {}

  @Test
  public void testSubstituteWithoutNestedVariable() {
    List<TypeVariable> parameters =
        GenericClassType.forClass(OtherNestedArgumentBound.class).getTypeParameters();
    TypeVariable u = parameters.get(0);
    TypeVariable t = parameters.get(1);
    ParameterBound bound = t.getUpperTypeBound();
    assertTrue(bound instanceof LazyParameterBound);
    // The substitution does not map U, which occurs in the bound.  U remains a type variable in
    // the result, rather than becoming a null type argument.
    ParameterBound substituted = bound.substitute(new Substitution(t, JavaTypes.STRING_TYPE));
    assertTrue(
        "bound " + substituted + " should be lazy since it contains a type variable",
        substituted instanceof LazyReferenceBound);
    assertEquals(
        GenericClassType.forClass(Map.class)
            .instantiate(
                GenericClassType.forClass(List.class).instantiate(u), JavaTypes.STRING_TYPE),
        ((ReferenceBound) substituted).getBoundType());
  }

  /**
   * A type parameter whose self-referential bound contains an array of the type parameter.
   *
   * @param <T> the type parameter
   */
  static class ArrayArgumentBound<T extends List<T[]>> {}

  @Test
  public void testSubstituteNullTypeForArrayComponent() {
    TypeVariable t = GenericClassType.forClass(ArrayArgumentBound.class).getTypeParameters().get(0);
    ParameterBound bound = t.getUpperTypeBound();
    assertTrue(bound instanceof LazyParameterBound);
    ParameterBound substituted = bound.substitute(new Substitution(t, JavaTypes.NULL_TYPE));
    assertEquals(
        GenericClassType.forClass(List.class)
            .instantiate(ArrayType.ofComponentType(JavaTypes.NULL_TYPE)),
        ((ReferenceBound) substituted).getBoundType());
  }

  @Test
  public void testNullTypeCannotInstantiateArrayComponentBound() {
    TypeVariable t = GenericClassType.forClass(ArrayArgumentBound.class).getTypeParameters().get(0);
    // The null type is not a subtype of the parameterized type List<NullType[]>, just as it is not
    // a subtype of List<NullType> for a bound such as T extends List<T>.
    assertFalse(t.canBeInstantiatedBy(JavaTypes.NULL_TYPE));
  }

  @Test
  public void testReferenceBoundWithNullTypeForArrayComponent() {
    TypeVariable t = GenericClassType.forClass(ArrayArgumentBound.class).getTypeParameters().get(0);
    ReferenceBound bound = ReferenceBound.forTypeLazyIfGeneric(ArrayType.ofComponentType(t));
    assertTrue(bound instanceof LazyReferenceBound);
    Substitution nullSubstitution = new Substitution(t, JavaTypes.NULL_TYPE);
    ArrayType stringArray = ArrayType.ofComponentType(JavaTypes.STRING_TYPE);
    // The substituted bound is NullType[], which is a subtype of String[].  String[] is not a
    // subtype of NullType[].
    assertTrue(bound.isLowerBound(stringArray, nullSubstitution));
    assertFalse(bound.isUpperBound(stringArray, nullSubstitution));
    assertFalse(bound.isUpperBound(new EagerReferenceBound(stringArray), nullSubstitution));
  }

  /**
   * A type parameter whose bound contains, in a type argument, an array of the type parameter, and
   * another type parameter.
   *
   * @param <K> the other type parameter
   * @param <U> the type parameter with the bound
   */
  static class NestedArrayArgumentBound<K, U extends Map<K, U[]>> {}

  @Test
  public void testSubstituteNullTypeForNestedArrayComponent() {
    List<TypeVariable> parameters =
        GenericClassType.forClass(NestedArrayArgumentBound.class).getTypeParameters();
    TypeVariable k = parameters.get(0);
    TypeVariable u = parameters.get(1);
    // Substituting for K leaves the bound lazy:  Map<String, U[]>.
    ParameterBound bound =
        u.getUpperTypeBound().substitute(new Substitution(k, JavaTypes.STRING_TYPE));
    assertTrue(bound instanceof LazyReferenceBound);
    // Substituting the null type for U resolves the bound to Map<String, NullType[]>.
    Substitution nullSubstitution = new Substitution(u, JavaTypes.NULL_TYPE);
    assertEquals(
        GenericClassType.forClass(Map.class)
            .instantiate(JavaTypes.STRING_TYPE, ArrayType.ofComponentType(JavaTypes.NULL_TYPE)),
        ((ReferenceBound) bound.substitute(nullSubstitution)).getBoundType());
    // As in testNullTypeCannotInstantiateArrayComponentBound, the null type is not within the
    // parameterized bound.  Neither comparison throws an exception.
    assertFalse(bound.isUpperBound(JavaTypes.NULL_TYPE, nullSubstitution));
    assertFalse(bound.isLowerBound(JavaTypes.NULL_TYPE, nullSubstitution));
  }

  /**
   * A type parameter whose bound has a wildcard bounded by an array of another type parameter.
   *
   * @param <T> the component type
   * @param <U> the type parameter with the bound
   */
  static class WildcardArrayOfSiblingBound<T, U extends List<? extends T[]>> {}

  /**
   * A type parameter, to substitute for another one.
   *
   * @param <V> the type parameter
   */
  static class OneParameter<V> {}

  @Test
  public void testSubstituteVariableThenNullTypeForArrayComponent() {
    List<TypeVariable> parameters =
        GenericClassType.forClass(WildcardArrayOfSiblingBound.class).getTypeParameters();
    TypeVariable t = parameters.get(0);
    TypeVariable u = parameters.get(1);
    TypeVariable v = GenericClassType.forClass(OneParameter.class).getTypeParameters().get(0);
    ParameterBound bound = u.getUpperTypeBound().substitute(new Substitution(t, v));
    // Substituting the null type resolves the array bound, as substituting String does.
    ParameterBound nullBound = bound.substitute(new Substitution(v, JavaTypes.NULL_TYPE));
    assertEquals(
        u.getUpperTypeBound().substitute(new Substitution(t, JavaTypes.NULL_TYPE)), nullBound);
    ParameterBound stringBound = bound.substitute(new Substitution(v, JavaTypes.STRING_TYPE));
    assertEquals(
        u.getUpperTypeBound().substitute(new Substitution(t, JavaTypes.STRING_TYPE)), stringBound);
  }

  /**
   * A type parameter whose bound contains an array of a parameterized type with a concrete array
   * type argument.
   *
   * @param <T> the type parameter
   */
  static class ConcreteArrayArgumentBound<T extends List<Map<T, String[]>[]>> {}

  /** A class that satisfies the bound of {@link ConcreteArrayArgumentBound}. */
  @SuppressWarnings("serial")
  static class ConcreteArrayArgumentList
      extends ArrayList<Map<ConcreteArrayArgumentList, String[]>[]> {}

  @Test
  public void testConcreteArrayTypeArgument() {
    TypeVariable t =
        GenericClassType.forClass(ConcreteArrayArgumentBound.class).getTypeParameters().get(0);
    assertTrue(
        t.canBeInstantiatedBy(ClassOrInterfaceType.forClass(ConcreteArrayArgumentList.class)));
  }

  /** A non-generic class with a generic member class. */
  static class Plain {
    /**
     * A generic member class of a non-generic class.
     *
     * @param <U> the type parameter
     */
    class Inner<U> {}
  }

  /**
   * A type parameter whose bound is a generic member class of a non-generic class.
   *
   * @param <T> the type parameter
   */
  static class MemberOfPlainBound<T extends Plain.Inner<T>> {}

  /** A field whose type is the bound of {@link MemberOfPlainBound}, instantiated with String. */
  Plain.Inner<String> plainInnerString;

  @Test
  public void testSubstituteMemberClassOfNonGenericClass() throws NoSuchFieldException {
    TypeVariable t = GenericClassType.forClass(MemberOfPlainBound.class).getTypeParameters().get(0);
    ParameterBound bound = t.getUpperTypeBound();
    assertTrue(bound instanceof LazyParameterBound);
    ParameterBound substituted = bound.substitute(new Substitution(t, JavaTypes.STRING_TYPE));
    ClassOrInterfaceType expected =
        ClassOrInterfaceType.forType(
            LazyParameterBoundTest.class.getDeclaredField("plainInnerString").getGenericType());
    assertEquals(expected, ((ReferenceBound) substituted).getBoundType());
  }
}
