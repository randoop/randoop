package randoop.generation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static randoop.reflection.AccessibilityPredicate.IS_PUBLIC;

import java.util.ArrayList;
import org.junit.Test;
import org.plumelib.util.SIList;
import randoop.operation.TypedClassOperation;
import randoop.operation.TypedOperation;
import randoop.sequence.Sequence;
import randoop.sequence.SequenceCollection;
import randoop.sequence.Variable;
import randoop.types.ClassOrInterfaceType;
import randoop.types.Type;

/**
 * Tests that {@link DemandDrivenInputCreator} instantiates a generic producer whose output type is
 * not generic.
 */
public class DemandDrivenInputCreatorGenericProducerTest {

  /** The bound of the type parameter of {@link Foo#of}. */
  public interface Marker {}

  /** A class that implements {@link Marker}. */
  public static class MarkerImpl implements Marker {
    /** Creates a MarkerImpl. */
    public MarkerImpl() {}
  }

  /** A class whose only producer is a generic method with a non-generic output type. */
  public static class Foo {
    /** Creates a Foo; not accessible to Randoop. */
    private Foo() {}

    /**
     * Returns a new Foo.
     *
     * @param <T> the type of the argument
     * @param value a value, which is ignored
     * @return a new Foo
     */
    public static <T extends Marker> Foo of(T value) {
      return new Foo();
    }
  }

  /**
   * Returns a sequence collection that contains only a sequence that creates a {@link MarkerImpl}.
   *
   * @return a sequence collection that contains only a sequence that creates a {@link MarkerImpl}
   * @throws NoSuchMethodException if the constructor of {@link MarkerImpl} is not found
   */
  private static SequenceCollection markerImplCollection() throws NoSuchMethodException {
    SequenceCollection sequenceCollection = new SequenceCollection();
    TypedClassOperation markerConstructor =
        TypedOperation.forConstructor(MarkerImpl.class.getConstructor());
    sequenceCollection.add(new Sequence().extend(markerConstructor, new ArrayList<Variable>()));
    return sequenceCollection;
  }

  @Test
  public void testGenericProducerWithNonGenericOutput() throws Exception {
    DemandDrivenInputCreator creator =
        new DemandDrivenInputCreator(markerImplCollection(), IS_PUBLIC);
    Type fooType = ClassOrInterfaceType.forClass(Foo.class);

    SIList<Sequence> sequences = creator.createSequencesForType(fooType, false, false);
    assertFalse(sequences.isEmpty());
    for (Sequence sequence : sequences) {
      assertFalse(sequence.getLastStatement().getOperation().isGeneric());
    }
  }

  /**
   * Tests that a generic producer is skipped, without an exception, when the main sequence
   * collection contains no type that its type variable can be instantiated to.
   */
  @Test
  public void testGenericProducerWithoutCandidateType() {
    DemandDrivenInputCreator creator =
        new DemandDrivenInputCreator(new SequenceCollection(), IS_PUBLIC);
    Type fooType = ClassOrInterfaceType.forClass(Foo.class);

    SIList<Sequence> sequences = creator.createSequencesForType(fooType, false, false);
    assertTrue(sequences.isEmpty());
  }
}
