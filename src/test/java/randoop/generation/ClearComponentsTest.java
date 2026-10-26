package randoop.generation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static randoop.reflection.AccessibilityPredicate.IS_PUBLIC;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.plumelib.util.SIList;
import randoop.condition.RandoopSpecificationError;
import randoop.main.GenInputsAbstract;
import randoop.main.OptionsCache;
import randoop.operation.TypedOperation;
import randoop.reflection.DefaultReflectionPredicate;
import randoop.reflection.OmitMethodsPredicate;
import randoop.reflection.OperationExtractor;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Sequence;
import randoop.test.DummyCheckGenerator;
import randoop.types.ClassOrInterfaceType;
import randoop.util.Randomness;

/** Tests of {@link ForwardGenerator#clearComponents} and the state that it resets. */
public class ClearComponentsTest {

  /** A class under test whose method creates new primitive values. */
  public static class Adder {
    /**
     * Returns the sum of the arguments.
     *
     * @param x an integer
     * @param y an integer
     * @return the sum of the arguments
     */
    public static int add(int x, int y) {
      return x + y;
    }
  }

  private static OptionsCache optionsCache;

  @BeforeClass
  public static void setup() {
    optionsCache = new OptionsCache();
    optionsCache.saveState();
  }

  @AfterClass
  public static void restore() {
    optionsCache.restoreState();
  }

  /** Primitive values that were in the component set before a clear can be added after it. */
  @Test
  public void primitivesAreAddedAgainAfterClear() throws Exception {
    Set<Sequence> seeds = SeedSequences.defaultSeeds();
    ComponentManager componentMgr = new ComponentManager(seeds, IS_PUBLIC);
    ForwardGenerator gen = createGenerator(Adder.class, componentMgr);

    for (int i = 0; i < 100; i++) {
      gen.step();
    }
    Set<Sequence> primitivesBeforeClear = nonSeedPrimitives(componentMgr, seeds);
    assertFalse(primitivesBeforeClear.isEmpty());

    gen.clearComponents();
    assertTrue(nonSeedPrimitives(componentMgr, seeds).isEmpty());

    for (int i = 0; i < 100; i++) {
      gen.step();
    }
    Set<Sequence> primitivesAfterClear = nonSeedPrimitives(componentMgr, seeds);
    primitivesAfterClear.retainAll(primitivesBeforeClear);
    assertFalse(primitivesAfterClear.isEmpty());
  }

  /**
   * When a clear does not reduce memory use below {@code --clear-memory}, Randoop does not clear
   * the component set again on the next steps.
   */
  @Test
  public void clearForMemoryRaisesThreshold() throws Exception {
    long savedClearMemory = GenInputsAbstract.clear_memory;
    try {
      // Every clear leaves more memory in use than this.
      GenInputsAbstract.clear_memory = 1;
      Set<Sequence> seeds = SeedSequences.defaultSeeds();
      ComponentManager componentMgr = new ComponentManager(seeds, IS_PUBLIC);
      ForwardGenerator gen = createGenerator(Adder.class, componentMgr);

      for (int i = 0; i < 100; i++) {
        gen.step();
      }
      // If Randoop cleared the component set on every step, it would hold at most 2 generated
      // sequences: the last one and the primitive value that it created.
      assertTrue(componentMgr.getAllGeneratedSequences().size() > seeds.size() + 10);
    } finally {
      GenInputsAbstract.clear_memory = savedClearMemory;
    }
  }

  /** Tests {@link ForwardGenerator#computeClearMemoryThreshold}. */
  @Test
  public void computeClearMemoryThreshold() {
    long savedClearMemory = GenInputsAbstract.clear_memory;
    try {
      GenInputsAbstract.clear_memory = 1000;
      // The clear reduced memory use to at most --clear-memory.
      assertEquals(1000, ForwardGenerator.computeClearMemoryThreshold(400, 5000));
      assertEquals(1000, ForwardGenerator.computeClearMemoryThreshold(1000, 5000));
      // The clear did not reduce memory use to --clear-memory.
      assertEquals(3000, ForwardGenerator.computeClearMemoryThreshold(2000, 4000));
      assertEquals(4500, ForwardGenerator.computeClearMemoryThreshold(4000, 5000));
      assertEquals(
          Long.MAX_VALUE / 2 + 1000,
          ForwardGenerator.computeClearMemoryThreshold(2000, Long.MAX_VALUE));
    } finally {
      GenInputsAbstract.clear_memory = savedClearMemory;
    }
  }

  /** Clearing the component set keeps Orienteering's details of the retained sequences only. */
  @Test
  public void orienteeringRetainsDetailsOfRetainedSequences() {
    Sequence seed = Sequence.createSequenceForPrimitive(1);
    Sequence generated = Sequence.createSequenceForPrimitive(2);
    OrienteeringSelection selector = new OrienteeringSelection(Collections.singleton(seed));
    selector.createdExecutableSequence(new ExecutableSequence(generated));
    // Selecting the seed changes its weight.
    selector.selectInputSequence(SIList.from(Collections.singletonList(seed)));
    Double seedWeight = selector.getWeight(seed);
    assertNotNull(seedWeight);

    selector.componentsCleared(Collections.singleton(seed));
    assertEquals(seedWeight, selector.getWeight(seed));
    assertNull(selector.getWeight(generated));
  }

  /** Clearing the component set keeps the small-tests weights of the retained sequences only. */
  @Test
  public void smallTestsRetainsWeightsOfRetainedSequences() {
    Sequence seed = Sequence.createSequenceForPrimitive(1);
    Sequence generated = Sequence.createSequenceForPrimitive(2);
    SmallTestsSequenceSelection selector = new SmallTestsSequenceSelection();
    selector.selectInputSequence(SIList.from(Arrays.asList(seed, generated)));
    assertNotNull(selector.getWeight(seed));
    assertNotNull(selector.getWeight(generated));

    selector.componentsCleared(Collections.singleton(seed));
    assertNotNull(selector.getWeight(seed));
    assertNull(selector.getWeight(generated));
  }

  /**
   * Returns a generator for the given class.
   *
   * @param c the class under test
   * @param componentMgr the component set
   * @return a generator for the given class
   * @throws RandoopSpecificationError if a specification of the class cannot be evaluated
   */
  private static ForwardGenerator createGenerator(Class<?> c, ComponentManager componentMgr)
      throws RandoopSpecificationError {
    Randomness.setSeed(0);
    GenInputsAbstract.literals_level = GenInputsAbstract.ClassLiteralsMode.NONE;

    ClassOrInterfaceType classType = ClassOrInterfaceType.forClass(c);
    List<TypedOperation> operations =
        new ArrayList<>(
            OperationExtractor.operations(
                classType,
                new DefaultReflectionPredicate(new HashSet<>()),
                OmitMethodsPredicate.NO_OMISSION,
                IS_PUBLIC));
    ForwardGenerator gen =
        new ForwardGenerator(
            operations,
            new LinkedHashSet<>(),
            new GenInputsAbstract.Limits(),
            componentMgr,
            Collections.singleton(classType));
    gen.setTestCheckGenerator(new DummyCheckGenerator());
    return gen;
  }

  /**
   * Returns the sequences in the component set that create a primitive value and are not seeds.
   *
   * @param componentMgr the component set
   * @param seeds the seed sequences
   * @return the non-seed primitive sequences in the component set
   */
  private static Set<Sequence> nonSeedPrimitives(
      ComponentManager componentMgr, Set<Sequence> seeds) {
    Set<Sequence> result = new HashSet<>();
    for (Sequence s : componentMgr.getAllGeneratedSequences()) {
      if (s.size() == 1 && s.getLastVariable().getType().isPrimitive() && !seeds.contains(s)) {
        result.add(s);
      }
    }
    return result;
  }
}
