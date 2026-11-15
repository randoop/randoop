package randoop.types;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.Test;

/**
 * Tests subtyping between parameterized types whose type arguments are wildcards, which uses the
 * containment relation of <a
 * href="https://docs.oracle.com/javase/specs/jls/se17/html/jls-4.html#jls-4.5.1">JLS section
 * 4.5.1</a>.
 */
public class WildcardContainmentTest {

  /**
   * A generic class whose type parameter has a bound.
   *
   * @param <T> the type parameter
   */
  public static class NumberBox<T extends Number> {}

  /**
   * A generic class whose type parameter has an intersection bound.
   *
   * @param <T> the type parameter
   */
  public static class IntersectionBox<T extends Number & Runnable> {}

  /**
   * A generic class with generic member classes.
   *
   * @param <T> the type parameter
   */
  public static class Outer<T> {
    /**
     * A generic member class.
     *
     * @param <U> the type parameter
     */
    public class Inner1<U> {}

    /**
     * Another generic member class.
     *
     * @param <U> the type parameter
     */
    public class Inner2<U> {}
  }

  /**
   * A generic subclass of {@code ArrayList}.
   *
   * @param <X> the type parameter
   */
  public static class Child<X> extends ArrayList<X> {
    /** ID for serializing this class. */
    private static final long serialVersionUID = 20261110;
  }

  /** A field of type {@code Outer<String>.Inner1<Integer>}. */
  public Outer<String>.Inner1<Integer> inner1;

  /** A field of type {@code Outer<String>.Inner2<Integer>}. */
  public Outer<String>.Inner2<Integer> inner2;

  /** A field of type {@code ArrayList<?>}. */
  public ArrayList<?> arrayListQ;

  /**
   * Returns {@code Child<? extends T[]>}, whose wildcard has a lazy bound.
   *
   * @param <T> the type parameter
   * @return null
   */
  public <T> Child<? extends T[]> childExtendsArray() {
    return null;
  }

  /**
   * Returns {@code List<? extends U>}, where the bound of {@code U} is an unbounded type variable.
   *
   * @param <T> an unbounded type parameter
   * @param <U> a type parameter whose bound is {@code T}
   * @return null
   */
  public <T, U extends T> List<? extends U> listExtendsUnboundedVariable() {
    return null;
  }

  /**
   * Returns {@code List<U>}, where the bound of {@code U} is an unbounded type variable.
   *
   * @param <T> an unbounded type parameter
   * @param <U> a type parameter whose bound is {@code T}
   * @return null
   */
  public <T, U extends T> List<U> listUnboundedVariable() {
    return null;
  }

  /**
   * Returns {@code List<? extends U>}, where the bound of {@code U} is a type variable whose bound
   * is {@code Integer}.
   *
   * @param <T> a type parameter whose bound is {@code Integer}
   * @param <U> a type parameter whose bound is {@code T}
   * @return null
   */
  public <T extends Integer, U extends T> List<? extends U> listExtendsIntegerVariable() {
    return null;
  }

  /**
   * Returns {@code List<U>}, where the bound of {@code U} is a type variable whose bound is {@code
   * Integer}.
   *
   * @param <T> a type parameter whose bound is {@code Integer}
   * @param <U> a type parameter whose bound is {@code T}
   * @return null
   */
  public <T extends Integer, U extends T> List<U> listIntegerVariable() {
    return null;
  }

  /** A field of type {@code List<Integer>}. */
  public List<Integer> listInteger;

  /** A field of type {@code List<Number>}. */
  public List<Number> listNumber;

  /** A field of type {@code List<Object>}. */
  public List<Object> listObject;

  /** A field of type {@code List<?>}. */
  public List<?> listQ;

  /** A field of type {@code List<? extends Number>}. */
  public List<? extends Number> listExtendsNumber;

  /** A field of type {@code List<? extends Integer>}. */
  public List<? extends Integer> listExtendsInteger;

  /** A field of type {@code List<? super Integer>}. */
  public List<? super Integer> listSuperInteger;

  /** A field of type {@code List<? super Number>}. */
  public List<? super Number> listSuperNumber;

  /** A field of type {@code List<List<Integer>>}. */
  public List<List<Integer>> listListInteger;

  /** A field of type {@code List<List<?>>}. */
  public List<List<?>> listListQ;

  /** A field of type {@code ArrayList<Integer>}. */
  public ArrayList<Integer> arrayListInteger;

  /** A field of type {@code Collection<?>}. */
  public Collection<?> collectionQ;

  /** A field of type {@code Collection<? extends Number>}. */
  public Collection<? extends Number> collectionExtendsNumber;

  /** A field of type {@code Collection<String>}. */
  public Collection<String> collectionString;

  /** A field of type {@code List<String[]>}. */
  public List<String[]> listStringArray;

  /** A field of type {@code List<Object[]>}. */
  public List<Object[]> listObjectArray;

  /** A field of type {@code List<? extends String[]>}. */
  public List<? extends String[]> listExtendsStringArray;

  /** A field of type {@code List<? extends Object[]>}. */
  public List<? extends Object[]> listExtendsObjectArray;

  /** A field of type {@code List<? super String[]>}. */
  public List<? super String[]> listSuperStringArray;

  /** A field of type {@code Enum<?>}. */
  public Enum<?> enumQ;

  /** A field of type {@code Enum<? extends Enum<?>>}. */
  public Enum<? extends Enum<?>> enumExtendsEnumQ;

  /** A field of type {@code IntersectionBox<?>}. */
  public IntersectionBox<?> intersectionBoxQ;

  /** A field of type {@code IntersectionBox<? extends Number>}. */
  public IntersectionBox<? extends Number> intersectionBoxExtendsNumber;

  /** A field of type {@code NumberBox<?>}. */
  public NumberBox<?> numberBoxQ;

  /** A field of type {@code NumberBox<? extends Number>}. */
  public NumberBox<? extends Number> numberBoxExtendsNumber;

  /**
   * Returns the type of the field of this class that has the given name.
   *
   * @param fieldName the name of a field of this class
   * @return the type of the field
   */
  private static Type type(String fieldName) {
    try {
      return Type.forType(WildcardContainmentTest.class.getField(fieldName).getGenericType());
    } catch (NoSuchFieldException e) {
      throw new Error(e);
    }
  }

  /**
   * Returns true if the type of one field is a subtype of the type of another field.
   *
   * @param subField the name of the field whose type is the possible subtype
   * @param superField the name of the field whose type is the possible supertype
   * @return true if the type of {@code subField} is a subtype of the type of {@code superField}
   */
  private static boolean isSubtype(String subField, String superField) {
    return type(subField).isSubtypeOfOrEqualTo(type(superField));
  }

  /**
   * Returns the return type of the method of this class that has the given name.
   *
   * @param methodName the name of a method of this class that has no parameters
   * @return the return type of the method
   */
  private static Type returnType(String methodName) {
    try {
      return Type.forType(
          WildcardContainmentTest.class.getMethod(methodName).getGenericReturnType());
    } catch (NoSuchMethodException e) {
      throw new Error(e);
    }
  }

  @Test
  public void typeArgumentInWildcardTest() {
    assertTrue(isSubtype("listInteger", "listQ"));
    assertTrue(isSubtype("listInteger", "listExtendsNumber"));
    assertTrue(isSubtype("listInteger", "listSuperInteger"));
    assertTrue(isSubtype("listNumber", "listSuperInteger"));
    assertTrue(isSubtype("listObject", "listSuperNumber"));
    assertFalse(isSubtype("listObject", "listExtendsNumber"));
    assertFalse(isSubtype("listInteger", "listSuperNumber"));
    assertFalse(isSubtype("listInteger", "listNumber"));
  }

  @Test
  public void wildcardInWildcardTest() {
    assertTrue(isSubtype("listQ", "listQ"));
    assertTrue(isSubtype("listExtendsInteger", "listExtendsNumber"));
    assertTrue(isSubtype("listExtendsNumber", "listQ"));
    assertTrue(isSubtype("listSuperInteger", "listQ"));
    assertTrue(isSubtype("listSuperNumber", "listSuperInteger"));
    assertFalse(isSubtype("listSuperInteger", "listSuperNumber"));
    assertFalse(isSubtype("listQ", "listExtendsNumber"));
    assertFalse(isSubtype("listQ", "listSuperInteger"));
    assertFalse(isSubtype("listExtendsNumber", "listExtendsInteger"));
  }

  @Test
  public void nestedTypeArgumentTest() {
    // A type argument that is not a wildcard contains only itself.
    assertFalse(isSubtype("listListInteger", "listListQ"));
    assertTrue(isSubtype("listListQ", "listListQ"));
  }

  @Test
  public void supertypeTest() {
    assertTrue(isSubtype("arrayListInteger", "collectionQ"));
    assertTrue(isSubtype("arrayListInteger", "collectionExtendsNumber"));
    assertFalse(isSubtype("arrayListInteger", "collectionString"));
  }

  @Test
  public void distinctMemberClassTest() {
    assertFalse(isSubtype("inner1", "inner2"));
    assertFalse(isSubtype("inner2", "inner1"));
    assertTrue(isSubtype("inner1", "inner1"));
  }

  @Test
  public void lazyBoundTest() throws NoSuchMethodException {
    Type childExtendsArray =
        Type.forType(
            WildcardContainmentTest.class.getMethod("childExtendsArray").getGenericReturnType());
    assertTrue(childExtendsArray.isSubtypeOfOrEqualTo(type("arrayListQ")));
  }

  @Test
  public void captureConversionTest() {
    // The capture variable of NumberBox<?> has upper bound Number, from the type parameter.
    assertTrue(isSubtype("numberBoxQ", "numberBoxExtendsNumber"));
    assertTrue(isSubtype("numberBoxExtendsNumber", "numberBoxQ"));
  }

  @Test
  public void recursiveBoundCaptureTest() {
    // The capture variable C of Enum<?> has upper bound Enum<C>, which is a subtype of Enum<?>.
    assertTrue(isSubtype("enumQ", "enumExtendsEnumQ"));
  }

  @Test
  public void intersectionBoundCaptureTest() {
    // The capture variable of IntersectionBox<?> has upper bound Number & Runnable.
    assertTrue(isSubtype("intersectionBoxQ", "intersectionBoxExtendsNumber"));
  }

  @Test
  public void typeVariableBoundTest() {
    Type listExtendsNumber = type("listExtendsNumber");
    // U extends T, where T is unbounded:  U is not a subtype of Number.
    assertFalse(returnType("listExtendsUnboundedVariable").isSubtypeOfOrEqualTo(listExtendsNumber));
    assertFalse(returnType("listUnboundedVariable").isSubtypeOfOrEqualTo(listExtendsNumber));
    // U extends T, where T extends Integer:  U is a subtype of Number.
    assertTrue(returnType("listExtendsIntegerVariable").isSubtypeOfOrEqualTo(listExtendsNumber));
    assertTrue(returnType("listIntegerVariable").isSubtypeOfOrEqualTo(listExtendsNumber));
  }

  @Test
  public void arrayBoundTest() {
    assertTrue(isSubtype("listStringArray", "listExtendsStringArray"));
    assertTrue(isSubtype("listStringArray", "listExtendsObjectArray"));
    assertTrue(isSubtype("listStringArray", "listSuperStringArray"));
    assertTrue(isSubtype("listObjectArray", "listSuperStringArray"));
    assertTrue(isSubtype("listExtendsStringArray", "listExtendsObjectArray"));
    assertFalse(isSubtype("listObjectArray", "listExtendsStringArray"));
    assertFalse(isSubtype("listExtendsObjectArray", "listExtendsStringArray"));
  }
}
