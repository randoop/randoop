package randoop.generation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static randoop.reflection.AccessibilityPredicate.IS_PUBLIC;

import java.util.ArrayList;
import org.junit.Test;
import randoop.operation.TypedClassOperation;
import randoop.operation.TypedOperation;
import randoop.sequence.Sequence;
import randoop.sequence.SequenceCollection;
import randoop.sequence.Variable;
import randoop.types.Type;

/**
 * Tests that {@link DemandDrivenInputCreator} uses a generic producer, such as {@code static <T>
 * Box<T> of(T)}, to create a parameterized type, such as {@code Box<MarkerImpl>}.
 */
public class DemandDrivenInputCreatorParameterizedTargetTest {

  /** The bound of the type parameters of the classes in this test. */
  public interface Marker {}

  /** A class that implements {@link Marker}. */
  public static class MarkerImpl implements Marker {
    /** Creates a MarkerImpl. */
    public MarkerImpl() {}
  }

  /**
   * A holder of a value, whose only producer is a generic static method.
   *
   * @param <T> the type of the value
   */
  public static class Box<T extends Marker> {
    /** The value. */
    public final T value;

    /**
     * Creates a Box; not accessible to Randoop.
     *
     * @param value the value
     */
    private Box(T value) {
      this.value = value;
    }

    /**
     * Returns a Box that holds the given value.
     *
     * @param <T> the type of the value
     * @param value a value
     * @return a Box that holds {@code value}
     */
    public static <T extends Marker> Box<T> of(T value) {
      return new Box<>(value);
    }
  }

  /**
   * A holder of a value, whose only producer is a constructor.
   *
   * @param <T> the type of the value
   */
  public static class PublicBox<T extends Marker> {
    /** The value. */
    public final T value;

    /**
     * Creates a PublicBox.
     *
     * @param value the value
     */
    public PublicBox(T value) {
      this.value = value;
    }
  }

  /**
   * A holder of a value, whose only producer has a type parameter that does not occur in its return
   * type.
   *
   * @param <T> the type of the value
   */
  public static class TaggedBox<T extends Marker> {
    /** The value. */
    public final T value;

    /**
     * Creates a TaggedBox; not accessible to Randoop.
     *
     * @param value the value
     */
    private TaggedBox(T value) {
      this.value = value;
    }

    /**
     * Returns a TaggedBox that holds the given value.
     *
     * @param <T> the type of the value
     * @param <U> the type of the tag
     * @param value a value
     * @param tag a tag, which is ignored
     * @return a TaggedBox that holds {@code value}
     */
    public static <T extends Marker, U> TaggedBox<T> of(T value, U tag) {
      return new TaggedBox<>(value);
    }
  }

  /**
   * A pair, none of whose methods can produce a {@code Pair<String, MarkerImpl>}.
   *
   * @param <A> the type of the first element
   * @param <B> the type of the second element
   */
  public static class Pair<A, B extends Marker> {
    /** The first element. */
    public final A first;

    /** The second element. */
    public final B second;

    /**
     * Creates a Pair; not accessible to Randoop.
     *
     * @param first the first element
     * @param second the second element
     */
    private Pair(A first, B second) {
      this.first = first;
      this.second = second;
    }

    /**
     * Returns a Pair whose first element is 0.
     *
     * @param <B> the type of the second element
     * @param second the second element
     * @return a Pair of 0 and {@code second}
     */
    public static <B extends Marker> Pair<Integer, B> ofZero(B second) {
      return new Pair<>(0, second);
    }

    /**
     * Returns the given object, cast to an arbitrary type.
     *
     * @param <T> the type to cast to
     * @param o an object
     * @return {@code o}
     */
    @SuppressWarnings("unchecked")
    public static <T> T cast(Object o) {
      return (T) o;
    }
  }

  /** A field whose declared type is a parameterized type to create. */
  public Box<MarkerImpl> boxOfMarkerImpl;

  /** A field whose declared type is a parameterized type to create. */
  public PublicBox<MarkerImpl> publicBoxOfMarkerImpl;

  /** A field whose declared type is a parameterized type to create. */
  public TaggedBox<MarkerImpl> taggedBoxOfMarkerImpl;

  /** A field whose declared type is a parameterized type that cannot be created. */
  public Pair<String, MarkerImpl> pairOfStringAndMarkerImpl;

  /**
   * Returns a DemandDrivenInputCreator whose sequence collection can create a {@link MarkerImpl}.
   *
   * @return a DemandDrivenInputCreator whose sequence collection can create a {@link MarkerImpl}
   */
  private static DemandDrivenInputCreator newCreator() throws NoSuchMethodException {
    SequenceCollection sequenceCollection = new SequenceCollection();
    TypedClassOperation markerConstructor =
        TypedOperation.forConstructor(MarkerImpl.class.getConstructor());
    sequenceCollection.add(new Sequence().extend(markerConstructor, new ArrayList<Variable>()));
    return new DemandDrivenInputCreator(sequenceCollection, IS_PUBLIC);
  }

  /**
   * Returns the declared type of the given field of this class.
   *
   * @param fieldName the name of a field of this class
   * @return the declared type of the field named {@code fieldName}
   */
  private static Type fieldType(String fieldName) throws NoSuchFieldException {
    return Type.forType(
        DemandDrivenInputCreatorParameterizedTargetTest.class.getField(fieldName).getGenericType());
  }

  /**
   * Asserts that {@link DemandDrivenInputCreator} can create a value of the type of the given
   * field.
   *
   * @param fieldName the name of a field of this class
   */
  private static void assertCreatable(String fieldName) throws Exception {
    DemandDrivenInputCreator creator = newCreator();
    Type type = fieldType(fieldName);
    assertFalse(creator.createSequencesForType(type, false, false).isEmpty());
    assertFalse(creator.isUninstantiableType(type));
  }

  @Test
  public void testGenericStaticMethod() throws Exception {
    assertCreatable("boxOfMarkerImpl");
  }

  @Test
  public void testGenericClassConstructor() throws Exception {
    assertCreatable("publicBoxOfMarkerImpl");
  }

  @Test
  public void testTypeParameterOnlyInInputs() throws Exception {
    assertCreatable("taggedBoxOfMarkerImpl");
  }

  @Test
  public void testNoMatchingProducer() throws Exception {
    DemandDrivenInputCreator creator = newCreator();
    Type type = fieldType("pairOfStringAndMarkerImpl");
    assertTrue(creator.createSequencesForType(type, false, false).isEmpty());
  }
}
