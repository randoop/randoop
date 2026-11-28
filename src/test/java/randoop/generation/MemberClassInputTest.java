package randoop.generation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static randoop.reflection.AccessibilityPredicate.IS_PUBLIC;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Predicate;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.plumelib.util.SIList;
import randoop.DummyVisitor;
import randoop.condition.RandoopSpecificationError;
import randoop.main.GenInputsAbstract;
import randoop.main.GenTests;
import randoop.main.OptionsCache;
import randoop.operation.TypedOperation;
import randoop.reflection.DefaultReflectionPredicate;
import randoop.reflection.OmitMethodsPredicate;
import randoop.reflection.OperationExtractor;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Sequence;
import randoop.test.ContractSet;
import randoop.types.ArrayType;
import randoop.types.ClassOrInterfaceType;
import randoop.types.JavaTypes;
import randoop.types.Substitution;
import randoop.util.MultiMap;
import randoop.util.Randomness;

/**
 * Tests that input generation handles a member class of a parameterized type, such as {@code
 * Outer<String>.Inner}, which is parameterized but is not an {@link
 * randoop.types.InstantiatedType}.
 */
public class MemberClassInputTest {

  /** The saved values of Randoop's options. */
  private static OptionsCache optionsCache;

  /** Saves the values of Randoop's options. */
  @BeforeClass
  public static void setup() {
    optionsCache = new OptionsCache();
    optionsCache.saveState();
  }

  /** Restores the values of Randoop's options. */
  @AfterClass
  public static void restore() {
    optionsCache.restoreState();
  }

  /** {@code createArraySequence} creates an array of {@code Outer<String>.Inner}. */
  @Test
  public void createArraySequenceTest() {
    ClassOrInterfaceType genericInner =
        ClassOrInterfaceType.forClass(MemberClassInputs.Outer.Inner.class);
    ClassOrInterfaceType innerType =
        genericInner.substitute(
            new Substitution(genericInner.getTypeParameters(), JavaTypes.STRING_TYPE));
    assertTrue(innerType.isParameterized());
    ArrayType arrayType = ArrayType.ofComponentType(innerType);

    ComponentManager componentManager =
        new ComponentManager(SeedSequences.defaultSeeds(), IS_PUBLIC);
    for (int seed = 0; seed < 10; seed++) {
      Randomness.setSeed(seed);
      SIList<Sequence> sequences =
          HelperSequenceCreator.createArraySequence(componentManager, arrayType);
      for (Sequence sequence : sequences) {
        assertEquals(arrayType, sequence.getLastVariable().getType());
      }
    }
  }

  /**
   * {@link ForwardGenerator} creates inputs for methods whose parameter types are {@code
   * Outer<String>.Inner} and {@code Outer<String>.Inner[]}.
   *
   * <p>{@link OperationExtractor} gives the methods of {@link MemberClassInputs} the type parameter
   * {@code T} of {@code Outer}, as in {@code <T> consumeInner(Outer<T>.Inner)}. This test
   * substitutes {@code String} for {@code T} itself rather than relying on type instantiation.
   *
   * @throws RandoopSpecificationError if the specifications are invalid; never happens
   */
  @Test
  public void forwardGeneratorTest() throws RandoopSpecificationError {
    Randomness.setSeed(0);
    GenInputsAbstract.forbid_null = false;
    GenInputsAbstract.require_classname_in_test = null;
    // Otherwise, ForwardGenerator discards an operation whose input selection throws an exception.
    GenInputsAbstract.fail_on_generation_error = true;

    ClassOrInterfaceType classType = ClassOrInterfaceType.forClass(MemberClassInputs.class);
    List<TypedOperation> operations = new ArrayList<>();
    for (TypedOperation operation :
        OperationExtractor.operations(
            classType,
            new DefaultReflectionPredicate(new HashSet<>()),
            new OmitMethodsPredicate(GenInputsAbstract.omit_methods),
            IS_PUBLIC)) {
      if (operation.isGeneric()) {
        operation =
            operation.substitute(
                new Substitution(operation.getTypeParameters(), JavaTypes.STRING_TYPE));
      }
      operations.add(operation);
    }
    ComponentManager componentManager =
        new ComponentManager(SeedSequences.defaultSeeds(), IS_PUBLIC);
    ForwardGenerator gen =
        new ForwardGenerator(
            operations,
            new LinkedHashSet<TypedOperation>(),
            new GenInputsAbstract.Limits(0, 200, 200, 200),
            componentManager,
            /* stopper= */ null,
            Collections.singleton(classType));
    GenTests genTests = new GenTests();
    Predicate<ExecutableSequence> isOutputTest =
        genTests.createTestOutputPredicate(new HashSet<Sequence>(), new HashSet<Class<?>>(), null);
    gen.setTestPredicate(isOutputTest);
    gen.setTestCheckGenerator(
        GenTests.createTestCheckGenerator(
            IS_PUBLIC, new ContractSet(), new MultiMap<>(), OmitMethodsPredicate.NO_OMISSION));
    gen.setExecutionVisitor(new DummyVisitor());
    gen.createAndClassifySequences();

    boolean calledConsumeInner = false;
    boolean calledConsumeInnerArray = false;
    for (Sequence s : gen.getAllSequences()) {
      String code = s.toCodeString();
      calledConsumeInner |= code.contains("consumeInner(");
      calledConsumeInnerArray |= code.contains("consumeInnerArray(");
    }
    assertTrue(calledConsumeInner);
    assertTrue(calledConsumeInnerArray);
  }
}
