package randoop.generation;

import static org.junit.Assert.assertEquals;
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
import randoop.types.GenericClassType;
import randoop.types.ReferenceType;
import randoop.types.Substitution;
import randoop.types.Type;
import randoop.types.TypeVariable;

/**
 * Tests that {@link DemandDrivenInputCreator} instantiates a generic producer whose output type is
 * not generic, and a producer with wildcard types but no type variable.
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
   * A generic class.
   *
   * @param <T> the type parameter, which is not used
   */
  public static class Box<T> {
    /** Creates a Box. */
    public Box() {}
  }

  /** A class whose only producer has a wildcard parameter type and no type variable. */
  public static class Bar {
    /** Creates a Bar; not accessible to Randoop. */
    private Bar() {}

    /**
     * Returns a new Bar.
     *
     * @param box a Box, which is ignored
     * @return a new Bar
     */
    public static Bar of(Box<?> box) {
      return new Bar();
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

  /**
   * Tests that a producer with a wildcard parameter type and no type variable is instantiated with
   * a {@link Box} type from the main sequence collection.
   */
  @Test
  public void testWildcardProducer() throws Exception {
    SequenceCollection sequenceCollection = markerImplCollection();
    ReferenceType markerImplType = ClassOrInterfaceType.forClass(MarkerImpl.class);
    TypeVariable boxParameter = GenericClassType.forClass(Box.class).getTypeParameters().get(0);
    TypedClassOperation boxConstructor =
        TypedOperation.forConstructor(Box.class.getConstructor())
            .substitute(new Substitution(boxParameter, markerImplType));
    sequenceCollection.add(new Sequence().extend(boxConstructor, new ArrayList<Variable>()));
    DemandDrivenInputCreator creator = new DemandDrivenInputCreator(sequenceCollection, IS_PUBLIC);
    Type barType = ClassOrInterfaceType.forClass(Bar.class);

    SIList<Sequence> sequences = creator.createSequencesForType(barType, false, false);
    assertFalse(sequences.isEmpty());
    for (Sequence sequence : sequences) {
      TypedOperation operation = sequence.getLastStatement().getOperation();
      assertEquals(barType, operation.getOutputType());
      assertFalse(operation.hasWildcardTypes());
    }
  }
}
