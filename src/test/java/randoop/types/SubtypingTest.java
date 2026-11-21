package randoop.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import org.junit.Test;

/**
 * Tests {@link Subtyping}, {@link Substitution#extend(Substitution)}, and the supertypes of member
 * classes of parameterized types.
 */
public class SubtypingTest {

  /**
   * A generic class with member classes.
   *
   * @param <T> the type parameter
   */
  @SuppressWarnings({"ClassCanBeStatic", "serial"})
  static class Outer<T> {
    /**
     * A generic member class.
     *
     * @param <X> the type parameter
     */
    class A<X> {}

    /**
     * Another generic member class, unrelated to {@link A}.
     *
     * @param <X> the type parameter
     */
    class B<X> {}

    /** A non-generic member class whose supertypes mention the type parameter {@code T}. */
    abstract class Inner extends ArrayList<T> implements Supplier<T> {}
  }

  /** Fields whose types are used in tests. */
  @SuppressWarnings("UnusedVariable") // the fields are read reflectively
  static class FieldTypes {
    /** A member class of a parameterized type, with a wildcard type argument. */
    Outer<String>.B<?> bOfWildcard;

    /** A non-generic member class of a parameterized type. */
    Outer<String>.Inner innerOfString;

    /** A list of a member class whose enclosing type is {@code Outer<Integer>}. */
    List<Outer<Integer>.A<Long>> listOfIntegerA;

    /** A list of a member class whose enclosing type is {@code Outer<Number>}. */
    List<Outer<Number>.A<Long>> listOfNumberA;

    /** A list whose type argument is an unbounded wildcard. */
    List<?> listOfUnbounded;

    /** A list whose type argument is a wildcard bounded below by {@code Number}. */
    List<? super Number> listOfSuperNumber;

    /** A list whose type argument is a wildcard bounded below by {@code Integer}. */
    List<? super Integer> listOfSuperInteger;
  }

  /**
   * Returns the type of the given field of {@link FieldTypes}.
   *
   * @param fieldName the name of the field
   * @return the type of the field
   * @throws NoSuchFieldException if there is no such field
   */
  private static ClassOrInterfaceType fieldType(String fieldName) throws NoSuchFieldException {
    return ClassOrInterfaceType.forType(
        FieldTypes.class.getDeclaredField(fieldName).getGenericType());
  }

  @Test
  public void testGenericClassIsSubtypeOfWildcardInstantiation() {
    GenericClassType comparable = JavaTypes.COMPARABLE_TYPE;
    TypeArgument wildcard =
        new WildcardArgument(
            new WildcardType(new EagerReferenceBound(JavaTypes.OBJECT_TYPE), true));
    InstantiatedType comparableOfWildcard =
        new InstantiatedType(comparable, Collections.singletonList(wildcard));
    // Comparable<E> is a subtype of Comparable<?>.
    assertTrue(Subtyping.isSubtypeOrEqual(comparable, comparableOfWildcard));
  }

  @Test
  public void testUnrelatedMemberClassesAreNotSubtypes() throws NoSuchFieldException {
    GenericClassType aType = (GenericClassType) ClassOrInterfaceType.forClass(Outer.A.class);
    InstantiatedType aOfStringInteger =
        aType.instantiate(JavaTypes.STRING_TYPE, JavaTypes.INT_TYPE.toBoxedPrimitive());
    ClassOrInterfaceType bOfWildcard = fieldType("bOfWildcard");
    assertTrue(bOfWildcard instanceof InstantiatedType);
    // Outer<String>.A<Integer> is not a subtype of Outer<String>.B<?>, though the enclosing types
    // are the same and ? contains Integer.
    assertFalse(Subtyping.isSubtypeOrEqual(aOfStringInteger, bOfWildcard));
  }

  @Test
  public void testSupertypesOfMemberOfParameterizedType() throws NoSuchFieldException {
    ClassOrInterfaceType innerOfString = fieldType("innerOfString");
    assertEquals(
        GenericClassType.forClass(ArrayList.class).instantiate(JavaTypes.STRING_TYPE),
        innerOfString.getSuperclass());
    assertEquals(
        GenericClassType.forClass(Supplier.class).instantiate(JavaTypes.STRING_TYPE),
        innerOfString.getInterfaces().get(0));
  }

  @Test
  public void testExtendKeepsExplicitReplacement() {
    TypeVariable t = GenericClassType.forClass(Outer.class).getTypeParameters().get(0);
    TypeVariable tCopy =
        t.createCopyWithBounds(
            t.getLowerTypeBound(), new EagerReferenceBound(JavaTypes.STRING_TYPE));
    Substitution identity = new Substitution(t, t);
    Substitution replacement = new Substitution(tCopy, t);
    // Either order of merging maps tCopy to t.
    assertSame(t, tCopy.substitute(identity.extend(replacement)));
    assertSame(t, tCopy.substitute(replacement.extend(identity)));
  }

  @Test
  public void testNonWildcardArgumentComparesEnclosingTypes() throws NoSuchFieldException {
    ClassOrInterfaceType listOfIntegerA = fieldType("listOfIntegerA");
    ClassOrInterfaceType listOfNumberA = fieldType("listOfNumberA");
    assertTrue(listOfIntegerA.isSubtypeOfOrEqualTo(listOfIntegerA));
    // Outer<Integer>.A<Long> and Outer<Number>.A<Long> are different types.
    assertFalse(listOfIntegerA.isSubtypeOfOrEqualTo(listOfNumberA));
    assertFalse(listOfNumberA.isSubtypeOfOrEqualTo(listOfIntegerA));
  }

  @Test
  public void testContainmentOfLowerBoundedWildcards() throws NoSuchFieldException {
    ClassOrInterfaceType listOfUnbounded = fieldType("listOfUnbounded");
    ClassOrInterfaceType listOfSuperNumber = fieldType("listOfSuperNumber");
    ClassOrInterfaceType listOfSuperInteger = fieldType("listOfSuperInteger");
    // ? contains ? super Number, but not the reverse.
    assertTrue(listOfSuperNumber.isSubtypeOfOrEqualTo(listOfUnbounded));
    assertFalse(listOfUnbounded.isSubtypeOfOrEqualTo(listOfSuperNumber));
    // ? super Integer contains ? super Number, but not the reverse.
    assertTrue(listOfSuperNumber.isSubtypeOfOrEqualTo(listOfSuperInteger));
    assertFalse(listOfSuperInteger.isSubtypeOfOrEqualTo(listOfSuperNumber));
  }
}
