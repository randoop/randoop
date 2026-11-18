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
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.plumelib.util.SIList;
import randoop.condition.RandoopSpecificationError;
import randoop.generation.literaltfidf.ScopeToLiteralStatistics;
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

  @After
  public void restore() {
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
   * Clearing the component set via the generator keeps each weighted selector's weights of literals
   * and discards its weights of generated sequences.
   */
  @Test
  public void generatorClearUpdatesSelectorWeights() throws Exception {
    checkGeneratorClearUpdatesSelectorWeights(GenInputsAbstract.InputSelectionMode.ORIENTEERING);
    checkGeneratorClearUpdatesSelectorWeights(GenInputsAbstract.InputSelectionMode.SMALL_TESTS);
  }

  /**
   * Checks that {@link ForwardGenerator#clearComponents} keeps the selector's weight of a literal
   * and discards its weight of a generated sequence.
   *
   * @param inputSelection the value for {@link GenInputsAbstract#input_selection}
   * @throws RandoopSpecificationError if a specification of the class cannot be evaluated
   */
  private static void checkGeneratorClearUpdatesSelectorWeights(
      GenInputsAbstract.InputSelectionMode inputSelection) throws RandoopSpecificationError {
    GenInputsAbstract.input_selection = inputSelection;
    Set<Sequence> seeds = SeedSequences.defaultSeeds();
    ComponentManager componentMgr = new ComponentManager(seeds, IS_PUBLIC);
    ForwardGenerator gen = createGenerator(Adder.class, componentMgr);
    InputSequenceSelector selector = gen.getInputSequenceSelector();

    for (int i = 0; i < 100; i++) {
      gen.step();
    }
    Set<Sequence> generatedSequences = nonSeedPrimitives(componentMgr, seeds);
    assertFalse(generatedSequences.isEmpty());
    Sequence generated = generatedSequences.iterator().next();

    GenInputsAbstract.literals_level = GenInputsAbstract.ClassLiteralsMode.CLASS;
    Sequence literal = Sequence.createSequenceForPrimitive(4242);
    assertFalse(generatedSequences.contains(literal));
    ScopeToLiteralStatistics literals = new ScopeToLiteralStatistics();
    literals.incrementNumUses(ClassOrInterfaceType.forClass(Adder.class), literal, 1);
    componentMgr.setScopeToLiteralStatistics(literals);

    // Selecting a sequence caches its weight (and, for Orienteering, changes it).
    selector.selectInputSequence(SIList.from(Collections.singletonList(literal)));
    selector.selectInputSequence(SIList.from(Collections.singletonList(generated)));
    Double literalWeight = getWeight(selector, literal);
    assertNotNull(literalWeight);
    assertNotNull(getWeight(selector, generated));

    gen.clearComponents();
    assertEquals(literalWeight, getWeight(selector, literal));
    assertNull(getWeight(selector, generated));
  }

  /**
   * Returns the weight that the given selector has cached for the given sequence, or null.
   *
   * @param selector an {@link OrienteeringSelection} or a {@link SmallTestsSequenceSelection}
   * @param sequence a sequence
   * @return the weight that the selector has cached for the sequence, or null
   */
  private static @Nullable Double getWeight(InputSequenceSelector selector, Sequence sequence) {
    if (selector instanceof OrienteeringSelection) {
      return ((OrienteeringSelection) selector).getWeight(sequence);
    } else if (selector instanceof SmallTestsSequenceSelection) {
      return ((SmallTestsSequenceSelection) selector).getWeight(sequence);
    } else {
      throw new IllegalArgumentException("Not a weighted selector: " + selector);
    }
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
    GenInputsAbstract.progressdisplay = false;

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
