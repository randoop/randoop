package randoop.generation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static randoop.reflection.AccessibilityPredicate.IS_PUBLIC;

import java.util.Collections;
import java.util.Set;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import randoop.generation.literaltfidf.ScopeToLiteralStatistics;
import randoop.main.GenInputsAbstract;
import randoop.main.OptionsCache;
import randoop.sequence.Sequence;
import randoop.types.ClassOrInterfaceType;

/** Tests of {@link ComponentManager}. */
public class ComponentManagerTest {

  private static OptionsCache optionsCache;

  @BeforeClass
  public static void setup() {
    optionsCache = new OptionsCache();
    optionsCache.saveState();
  }

  @After
  public void restore() {
    optionsCache.restoreState();
  }

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

  /** A seed prevents adding its value to the component set again. */
  @Test
  public void seedSuppressesGeneratedValue() {
    Sequence seed = Sequence.createSequenceForPrimitive(4242);
    ComponentManager componentMgr = new ComponentManager(Collections.singleton(seed), IS_PUBLIC);
    componentMgr.addPrimitiveValueIfNew(4242);
    assertEquals(1, componentMgr.getAllGeneratedSequences().size());
  }

  /** A literal does not prevent adding its value to the component set, at any literals level. */
  @Test
  public void literalDoesNotSuppressGeneratedValue() {
    assertTrue(addsValueThatIsALiteral(GenInputsAbstract.ClassLiteralsMode.CLASS));
    assertTrue(addsValueThatIsALiteral(GenInputsAbstract.ClassLiteralsMode.PACKAGE));
    assertTrue(addsValueThatIsALiteral(GenInputsAbstract.ClassLiteralsMode.ALL));
  }

  /**
   * Returns true if {@link ComponentManager#addPrimitiveValueIfNew} adds a value that is a literal
   * of {@link ComponentManagerTest} to the component set.
   *
   * @param literalsLevel the value for {@link GenInputsAbstract#literals_level}
   * @return true if {@link ComponentManager#addPrimitiveValueIfNew} adds the value
   */
  private static boolean addsValueThatIsALiteral(
      GenInputsAbstract.ClassLiteralsMode literalsLevel) {
    GenInputsAbstract.literals_level = literalsLevel;
    Sequence literal = Sequence.createSequenceForPrimitive(4242);
    ScopeToLiteralStatistics literals = new ScopeToLiteralStatistics();
    literals.incrementNumUses(
        ClassOrInterfaceType.forClass(ComponentManagerTest.class), literal, 1);
    ComponentManager componentMgr = new ComponentManager(IS_PUBLIC);
    componentMgr.setScopeToLiteralStatistics(literals);
    componentMgr.addPrimitiveValueIfNew(4242);
    return componentMgr.getAllGeneratedSequences().contains(literal);
  }
}
