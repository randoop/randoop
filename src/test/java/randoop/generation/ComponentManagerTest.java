package randoop.generation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static randoop.reflection.AccessibilityPredicate.IS_PUBLIC;

import java.util.Set;
import org.junit.Test;
import randoop.sequence.Sequence;

/** Tests of {@link ComponentManager#numGeneratedSequences()}. */
public class ComponentManagerTest {

  @Test
  public void testSeedsAreNotCounted() {
    Set<Sequence> seeds = SeedSequences.defaultSeeds();
    ComponentManager mgr = new ComponentManager(seeds, IS_PUBLIC);
    assertEquals(0, mgr.numGeneratedSequences());
    assertTrue(mgr.getAllGeneratedSequences().containsAll(seeds));
  }

  @Test
  public void testAddAndClear() {
    Set<Sequence> seeds = SeedSequences.defaultSeeds();
    ComponentManager mgr = new ComponentManager(seeds, IS_PUBLIC);
    Sequence s1 = Sequence.createSequenceForPrimitive(12345);
    Sequence s2 = Sequence.createSequenceForPrimitive("ComponentManagerTest");
    mgr.addGeneratedSequence(s1);
    assertEquals(1, mgr.numGeneratedSequences());
    mgr.addGeneratedSequence(s2);
    assertEquals(2, mgr.numGeneratedSequences());

    mgr.clearGeneratedSequences();
    assertEquals(0, mgr.numGeneratedSequences());
    Set<Sequence> afterClear = mgr.getAllGeneratedSequences();
    assertTrue(afterClear.containsAll(seeds));
    assertFalse(afterClear.contains(s1));
    assertFalse(afterClear.contains(s2));

    mgr.addGeneratedSequence(s1);
    assertEquals(1, mgr.numGeneratedSequences());
  }

  @Test
  public void testSequenceWithNoActiveOutputsIsNotCounted() {
    ComponentManager mgr = new ComponentManager(SeedSequences.defaultSeeds(), IS_PUBLIC);
    Sequence inactive = Sequence.createSequenceForPrimitive(54321);
    inactive.clearAllActiveFlags();
    mgr.addGeneratedSequence(inactive);
    assertEquals(0, mgr.numGeneratedSequences());
    assertFalse(mgr.getAllGeneratedSequences().contains(inactive));
  }
}
