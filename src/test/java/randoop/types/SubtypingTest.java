package randoop.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
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
  public void testIdentityIsInconsistentWithCopyReplacement() {
    TypeVariable t = GenericClassType.forClass(Outer.class).getTypeParameters().get(0);
    TypeVariable tCopy =
        t.createCopyWithBounds(
            t.getLowerTypeBound(), new EagerReferenceBound(JavaTypes.STRING_TYPE));
    Substitution identity = new Substitution(t, t);
    Substitution replacement = new Substitution(tCopy, t);
    // The identity mapping keeps tCopy, but the replacement replaces tCopy by t.  So, in either
    // order, the substitutions are inconsistent and cannot be merged.
    assertFalse(identity.isConsistentWith(replacement));
    assertFalse(replacement.isConsistentWith(identity));
    assertThrows(IllegalArgumentException.class, () -> identity.extend(replacement));
    assertThrows(IllegalArgumentException.class, () -> replacement.extend(identity));
  }
}
