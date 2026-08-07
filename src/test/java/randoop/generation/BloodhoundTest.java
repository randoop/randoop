package randoop.generation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import randoop.DummyVisitor;
import randoop.main.GenInputsAbstract;
import randoop.main.GenTests;
import randoop.main.OptionsCache;
import randoop.operation.TypedClassOperation;
import randoop.operation.TypedOperation;
import randoop.reflection.AccessibilityPredicate;
import randoop.reflection.DefaultReflectionPredicate;
import randoop.reflection.OmitMethodsPredicate;
import randoop.reflection.OperationExtractor;
import randoop.reflection.ReflectionPredicate;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Sequence;
import randoop.test.ContractSet;
import randoop.test.TestCheckGenerator;
import randoop.types.ClassOrInterfaceType;
import randoop.types.Type;
import randoop.util.MultiMap;
import randoop.util.Randomness;

/** Tests of {@link Bloodhound}. */
public class BloodhoundTest {

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

  /**
   * An {@link OperationHistoryLogInterface} that counts, for each operation, how many times {@link
   * ForwardGenerator} removed the operation from the methods under test.
   */
  private static class RemovalCounter implements OperationHistoryLogInterface {

    /** Maps an operation to the number of times it was removed from the methods under test. */
    final Map<TypedOperation, Integer> removalCounts = new LinkedHashMap<>();

    @Override
    public void add(TypedOperation operation, OperationOutcome outcome) {
      if (outcome == OperationOutcome.REMOVED) {
        removalCounts.merge(operation, 1, Integer::sum);
      }
    }

    @Override
    public void outputTable() {}
  }

  /**
   * A parameterless operation is removed from the methods under test the first time it is used, so
   * Bloodhound must never select it again.
   */
  @Test
  public void doesNotSelectRemovedOperations() {
    Randomness.setSeed(0);
    GenInputsAbstract.method_selection = GenInputsAbstract.MethodSelectionMode.BLOODHOUND;
    // The default, TIME, would make this test depend on how fast the machine is.
    GenInputsAbstract.bloodhound_update_mode =
        GenInputsAbstract.BloodhoundCoverageUpdateMode.INVOCATIONS;

    RemovalCounter removalCounter = new RemovalCounter();
    ForwardGenerator gen = buildGenerator(Flaky.class, removalCounter);
    gen.createAndClassifySequences();

    // The class under test has a no-argument constructor, so at least one operation is removed.
    assertFalse(removalCounter.removalCounts.isEmpty());
    for (Map.Entry<TypedOperation, Integer> entry : removalCounter.removalCounts.entrySet()) {
      assertEquals(
          "Operation "
              + entry.getKey()
              + " was selected after being removed from the methods under test",
          1,
          (int) entry.getValue());
    }
  }

  /**
   * Creates a generator for the given class under test, using Bloodhound to select operations.
   *
   * @param c the class under test
   * @param operationHistory the operation history logger for the generator
   * @return a generator for the given class under test
   */
  private static ForwardGenerator buildGenerator(
      Class<?> c, OperationHistoryLogInterface operationHistory) {
    AccessibilityPredicate accessibility = AccessibilityPredicate.IS_PUBLIC;
    ReflectionPredicate reflectionPredicate = new DefaultReflectionPredicate();
    ClassOrInterfaceType classType = ClassOrInterfaceType.forClass(c);
    Set<ClassOrInterfaceType> classesUnderTest = Collections.singleton(classType);

    Collection<TypedOperation> operations =
        OperationExtractor.operations(
            classType, reflectionPredicate, OmitMethodsPredicate.NO_OMISSION, accessibility);

    Collection<Sequence> components = new LinkedHashSet<>(SeedSequences.defaultSeeds());
    ComponentManager componentMgr = new ComponentManager(components, accessibility);
    ForwardGenerator gen =
        new ForwardGenerator(
            new ArrayList<>(operations),
            new LinkedHashSet<TypedOperation>(),
            new GenInputsAbstract.Limits(0, 500, 500, 500),
            componentMgr,
            /* stopper= */ null,
            classesUnderTest);

    GenTests genTests = new GenTests();
    Predicate<ExecutableSequence> isOutputTest =
        genTests.createTestOutputPredicate(new LinkedHashSet<>(), new LinkedHashSet<>(), null);
    gen.setTestPredicate(isOutputTest);
    TestCheckGenerator checkGenerator =
        GenTests.createTestCheckGenerator(
            accessibility,
            new ContractSet(),
            new MultiMap<Type, TypedClassOperation>(),
            OmitMethodsPredicate.NO_OMISSION);
    gen.setTestCheckGenerator(checkGenerator);
    gen.setExecutionVisitor(new DummyVisitor());
    gen.setOperationHistoryLogger(operationHistory);
    return gen;
  }
}
