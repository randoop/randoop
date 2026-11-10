package randoop.generation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static randoop.reflection.AccessibilityPredicate.IS_PUBLIC;

import java.util.ArrayList;
import java.util.Collections;
import org.junit.Test;
import org.plumelib.util.SIList;
import randoop.condition.RandoopSpecificationError;
import randoop.operation.TypedClassOperation;
import randoop.operation.TypedOperation;
import randoop.sequence.Sequence;
import randoop.sequence.Variable;
import randoop.types.ClassOrInterfaceType;
import randoop.types.Type;

/**
 * Tests that {@link ComponentManager#clearGeneratedSequences} removes generated sequences but
 * preserves seed sequences.
 */
public class ComponentManagerClearTest {

  /** A class whose instances are created by a seed sequence. */
  public static class Seeded {
    /** Creates a Seeded. */
    public Seeded() {}
  }

  /** A class whose instances are created by a generated sequence. */
  public static class Generated {
    /** Creates a Generated. */
    public Generated() {}
  }

  /**
   * Returns a sequence that calls the no-argument constructor of the given class.
   *
   * @param c a class with a public no-argument constructor
   * @return a sequence that constructs an instance of {@code c}
   */
  private static Sequence constructorSequence(Class<?> c)
      throws NoSuchMethodException, RandoopSpecificationError {
    TypedClassOperation constructor = TypedOperation.forConstructor(c.getConstructor());
    return new Sequence().extend(constructor, new ArrayList<Variable>());
  }

  @Test
  public void testSeedsSurviveClear() throws Exception {
    Sequence seed = constructorSequence(Seeded.class);
    Sequence generated = constructorSequence(Generated.class);
    Type seededType = ClassOrInterfaceType.forClass(Seeded.class);
    Type generatedType = ClassOrInterfaceType.forClass(Generated.class);

    ComponentManager mgr = new ComponentManager(Collections.singleton(seed), IS_PUBLIC);
    mgr.addGeneratedSequence(generated);
    assertTrue(mgr.getAllGeneratedSequences().contains(seed));
    assertTrue(mgr.getAllGeneratedSequences().contains(generated));
    assertEquals(1, mgr.getSequencesForType(generatedType).size());

    mgr.clearGeneratedSequences();

    // The seed sequence remains available for selection.
    SIList<Sequence> seededCandidates = mgr.getSequencesForType(seededType);
    assertEquals(1, seededCandidates.size());
    assertEquals(seed, seededCandidates.get(0));
    assertTrue(mgr.getAllGeneratedSequences().contains(seed));

    // The generated sequence has been removed.
    assertTrue(mgr.getSequencesForType(generatedType).isEmpty());
    assertFalse(mgr.getAllGeneratedSequences().contains(generated));
  }
}
