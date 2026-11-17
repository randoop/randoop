package randoop.types;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

/** Tests {@link ParameterBound}. */
public class ParameterBoundTest {

  /** A field whose type is a generic array type that contains no type variable. */
  @SuppressWarnings("unused")
  private static List<String>[] concreteGenericArray;

  /**
   * Returns the generic type of the given field of the given class.
   *
   * @param c a class
   * @param fieldName the name of a field declared in {@code c}
   * @return the generic type of the field
   */
  private static java.lang.reflect.Type fieldType(Class<?> c, String fieldName) {
    try {
      return c.getDeclaredField(fieldName).getGenericType();
    } catch (NoSuchFieldException e) {
      throw new Error(e);
    }
  }

  @Test
  public void testConcreteGenericArrayBoundWithEmptySubstitution() {
    java.lang.reflect.Type arrayType = fieldType(ParameterBoundTest.class, "concreteGenericArray");
    ParameterBound bound = ParameterBound.forType(Collections.emptySet(), arrayType);
    Type argType = Type.forType(arrayType);
    Substitution empty = new Substitution();
    assertTrue(bound.isUpperBound(argType, empty));
    assertTrue(bound.isLowerBound(argType, empty));
  }

  /**
   * A class whose fields have types that contain an array of its type parameter.
   *
   * @param <T> the type parameter
   */
  @SuppressWarnings("unused")
  private static class ArraysOfVariable<T> {
    /** A field whose type is an array of the type parameter. */
    T[] array;

    /** A field whose type has an array of the type parameter as a type argument. */
    List<T[]> listOfArrays;
  }

  /**
   * Returns true if the bound for the given field's type, created with no type variables bound, is
   * an upper bound of {@code Object} after the type parameter of {@link ArraysOfVariable} is
   * replaced by the null type.
   *
   * @param fieldName a simple name, such as {@code array}, of a field declared in {@link
   *     ArraysOfVariable}
   * @return true if the substituted bound is an upper bound of {@code Object}
   */
  private static boolean isUpperBoundWithNullComponent(String fieldName) {
    java.lang.reflect.Type type = fieldType(ArraysOfVariable.class, fieldName);
    ParameterBound bound = ParameterBound.forType(Collections.emptySet(), type);
    TypeVariable t = GenericClassType.forClass(ArraysOfVariable.class).getTypeParameters().get(0);
    return bound.isUpperBound(JavaTypes.OBJECT_TYPE, new Substitution(t, JavaTypes.NULL_TYPE));
  }

  @Test
  public void testArrayOfUnboundVariableWithNullComponent() {
    // There is no array of the null type, so no type satisfies the bound.
    assertFalse(isUpperBoundWithNullComponent("array"));
  }

  @Test
  public void testNestedArrayOfUnboundVariableWithNullComponent() {
    // There is no array of the null type, so no type satisfies the bound.
    assertFalse(isUpperBoundWithNullComponent("listOfArrays"));
  }

  /**
   * A generic class with an inner class.
   *
   * @param <T> the type parameter
   */
  @SuppressWarnings({"unused", "ClassCanBeStatic"})
  private static class Outer<T> {
    /**
     * An inner class, whose owner type has a type variable.
     *
     * @param <U> the type parameter
     */
    class Inner<U> {}

    /**
     * A field whose type is a generic array type in which a type variable occurs only in an owner
     * type.
     */
    Inner<String>[] innerArray;
  }

  @Test
  public void testGenericArrayWithVariableInOwnerType() {
    java.lang.reflect.Type arrayType = fieldType(Outer.class, "innerArray");
    ParameterBound bound = ParameterBound.forType(Collections.emptySet(), arrayType);
    Type argType = Type.forType(arrayType);
    Substitution empty = new Substitution();
    assertTrue(bound.isUpperBound(argType, empty));
    assertTrue(bound.isLowerBound(argType, empty));
  }

  /**
   * A generic class with a non-generic inner class.
   *
   * @param <T> the type parameter
   */
  @SuppressWarnings({"unused", "ClassCanBeStatic"})
  private static class GenericOuter<T> {
    /** An inner class that has no type parameter of its own. */
    class NonGenericInner {}
  }

  /**
   * A class whose type parameter is bounded by a non-generic member class of a generic class.
   *
   * @param <T> the type parameter
   */
  @SuppressWarnings("unused")
  private static class NonGenericMemberClassBound<T extends GenericOuter<String>.NonGenericInner> {}

  @Test
  public void testNonGenericMemberClassBound() {
    TypeVariable t =
        GenericClassType.forClass(NonGenericMemberClassBound.class).getTypeParameters().get(0);
    assertFalse(t.canBeInstantiatedBy(JavaTypes.STRING_TYPE));
  }

  /**
   * A field whose type is a generic array type of a non-generic member class of a generic class.
   */
  @SuppressWarnings("unused")
  private static GenericOuter<String>.NonGenericInner[] nonGenericInnerArray;

  @Test
  public void testArrayOfNonGenericMemberClassBound() {
    java.lang.reflect.Type arrayType = fieldType(ParameterBoundTest.class, "nonGenericInnerArray");
    ParameterBound bound = ParameterBound.forType(Collections.emptySet(), arrayType);
    Type argType = Type.forType(arrayType);
    Substitution empty = new Substitution();
    assertTrue(bound.isUpperBound(argType, empty));
    assertTrue(bound.isLowerBound(argType, empty));
    ArrayType stringArray = ArrayType.ofComponentType(JavaTypes.STRING_TYPE);
    assertFalse(bound.isUpperBound(stringArray, empty));
    assertFalse(bound.isLowerBound(stringArray, empty));
  }

  /**
   * A class whose second type parameter has a bound that contains an array of the first.
   *
   * @param <U> the type parameter whose array occurs in the bound of {@code T}
   * @param <T> the type parameter whose bound contains an array of {@code U}
   */
  @SuppressWarnings("unused")
  private static class ArrayOfOtherVariableBound<U, T extends List<U[]>> {}

  @Test
  public void testBoundWithArrayOfOtherVariable() {
    TypeVariable t =
        GenericClassType.forClass(ArrayOfOtherVariableBound.class).getTypeParameters().get(1);
    assertFalse(t.canBeInstantiatedBy(JavaTypes.STRING_TYPE));
  }

  /**
   * A class whose third type parameter has a bound that mentions the second, whose bound contains
   * an array of the first.
   *
   * @param <V> the type parameter whose array occurs in the bound of {@code U}
   * @param <U> the type parameter whose bound contains an array of {@code V}
   * @param <T> the type parameter whose bound mentions {@code U}
   */
  @SuppressWarnings("unused")
  private static class BoundMentioningArrayBound<V, U extends List<V[]>, T extends Collection<U>> {}

  @Test
  public void testBoundMentioningVariableWithArrayBound() {
    TypeVariable t =
        GenericClassType.forClass(BoundMentioningArrayBound.class).getTypeParameters().get(2);
    assertFalse(t.canBeInstantiatedBy(JavaTypes.STRING_TYPE));
  }

  /**
   * A class whose fields have the same type: a list of a wildcard bounded by a parameterized type
   * that contains an array of the type parameter.
   *
   * @param <U> the type parameter
   */
  @SuppressWarnings("unused")
  private static class WildcardWithArrayOfVariable<U> {
    /** A field whose type is a list of a wildcard bounded by a list of arrays of {@code U}. */
    List<? extends List<U[]>> first;

    /** A field whose type is the same as that of {@link #first}. */
    List<? extends List<U[]>> second;
  }

  @Test
  public void testWildcardBoundWithArrayOfVariable() {
    Type firstType = Type.forType(fieldType(WildcardWithArrayOfVariable.class, "first"));
    Type secondType = Type.forType(fieldType(WildcardWithArrayOfVariable.class, "second"));
    assertTrue(firstType.isSubtypeOfOrEqualTo(secondType));
  }

  /**
   * A type parameter whose bound has a wildcard bounded by an array of a parameterized type in
   * which no type variable occurs.
   *
   * @param <T> the type parameter
   */
  static class WildcardConcreteGenericArrayBound<T extends List<? extends List<String>[]>> {}

  /** A class that satisfies the bound of {@link WildcardConcreteGenericArrayBound}. */
  @SuppressWarnings("serial")
  static class ConcreteGenericArrayList extends ArrayList<List<String>[]> {}

  @Test
  public void testWildcardConcreteGenericArrayBound() {
    TypeVariable t =
        GenericClassType.forClass(WildcardConcreteGenericArrayBound.class)
            .getTypeParameters()
            .get(0);
    assertTrue(
        t.canBeInstantiatedBy(ClassOrInterfaceType.forClass(ConcreteGenericArrayList.class)));
  }
}
