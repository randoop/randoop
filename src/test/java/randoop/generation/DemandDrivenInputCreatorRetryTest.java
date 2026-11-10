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
import randoop.types.ClassOrInterfaceType;
import randoop.types.Type;

/**
 * Tests that {@link DemandDrivenInputCreator} records a type as uninstantiable only if the type has
 * no producers, and retries a type whose producers are not yet usable.
 */
public class DemandDrivenInputCreatorRetryTest {

  /** The bound of the type parameter of {@link Box}. */
  public interface Marker {}

  /** A class that implements {@link Marker}. */
  public static class MarkerImpl implements Marker {
    /** Creates a MarkerImpl. */
    public MarkerImpl() {}
  }

  /** A class whose only producer is generic. */
  public abstract static class Holder {
    /** Creates a Holder; not accessible to Randoop. */
    protected Holder() {}

    /**
     * Returns a Box that holds the given value.
     *
     * @param <T> the type of the value
     * @param value a value
     * @return a Box that holds {@code value}
     */
    public static <T extends Marker> Box<T> box(T value) {
      return new Box<>(value);
    }
  }

  /**
   * A Holder of a value.
   *
   * @param <T> the type of the value
   */
  public static class Box<T extends Marker> extends Holder {
    /** The value. */
    public final T value;

    /**
     * Creates a Box; not accessible to Randoop.
     *
     * @param value the value
     */
    Box(T value) {
      this.value = value;
    }
  }

  /** A class that has no producer. */
  public static class NoProducer {
    /** Creates a NoProducer; not accessible to Randoop. */
    private NoProducer() {}
  }

  @Test
  public void testRetryUntilProducerIsUsable() throws Exception {
    SequenceCollection sequenceCollection = new SequenceCollection();
    DemandDrivenInputCreator creator = new DemandDrivenInputCreator(sequenceCollection, IS_PUBLIC);
    Type holderType = ClassOrInterfaceType.forClass(Holder.class);

    // No Marker is known, so the generic producer Holder.box cannot be instantiated.
    assertTrue(creator.createSequencesForType(holderType, false, false).isEmpty());
    assertFalse(creator.isUninstantiableType(holderType));

    // Once a Marker is known, Holder.box is usable.
    TypedClassOperation markerConstructor =
        TypedOperation.forConstructor(MarkerImpl.class.getConstructor());
    sequenceCollection.add(new Sequence().extend(markerConstructor, new ArrayList<Variable>()));
    assertFalse(creator.createSequencesForType(holderType, false, false).isEmpty());
    assertFalse(creator.isUninstantiableType(holderType));
  }

  @Test
  public void testNoProducer() {
    DemandDrivenInputCreator creator =
        new DemandDrivenInputCreator(new SequenceCollection(), IS_PUBLIC);
    Type noProducerType = ClassOrInterfaceType.forClass(NoProducer.class);

    assertTrue(creator.createSequencesForType(noProducerType, false, false).isEmpty());
    assertTrue(creator.isUninstantiableType(noProducerType));
    assertTrue(creator.getUninstantiableTypes().contains(noProducerType));
  }
}
