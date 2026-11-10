package randoop.generation;

import static org.junit.Assert.assertFalse;
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

  /** The bound of the type parameter of {@link Box}. */
  public interface Marker {}

  /** A class that implements {@link Marker}. */
  public static class MarkerImpl implements Marker {
    /** Creates a MarkerImpl. */
    public MarkerImpl() {}
  }

  /**
   * A holder of a value, whose only producer is generic.
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

  /** A field whose declared type is the parameterized type to create. */
  public Box<MarkerImpl> boxOfMarkerImpl;

  @Test
  public void testParameterizedTarget() throws Exception {
    SequenceCollection sequenceCollection = new SequenceCollection();
    TypedClassOperation markerConstructor =
        TypedOperation.forConstructor(MarkerImpl.class.getConstructor());
    sequenceCollection.add(new Sequence().extend(markerConstructor, new ArrayList<Variable>()));
    DemandDrivenInputCreator creator = new DemandDrivenInputCreator(sequenceCollection, IS_PUBLIC);
    Type boxType =
        Type.forType(
            DemandDrivenInputCreatorParameterizedTargetTest.class
                .getField("boxOfMarkerImpl")
                .getGenericType());

    assertFalse(creator.createSequencesForType(boxType, false, false).isEmpty());
    assertFalse(creator.isUninstantiableType(boxType));
  }
}
