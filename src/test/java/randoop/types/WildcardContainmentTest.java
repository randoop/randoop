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
  public void captureConversionTest() {
    // The capture variable of NumberBox<?> has upper bound Number, from the type parameter.
    assertTrue(isSubtype("numberBoxQ", "numberBoxExtendsNumber"));
    assertTrue(isSubtype("numberBoxExtendsNumber", "numberBoxQ"));
  }
}
