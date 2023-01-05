package randoop.generation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static randoop.reflection.AccessibilityPredicate.IS_PUBLIC;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import org.checkerframework.checker.signature.qual.ClassGetName;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import randoop.DummyVisitor;
import randoop.ExceptionalExecution;
import randoop.ExecutionOutcome;
import randoop.main.GenInputsAbstract;
import randoop.main.GenInputsAbstract.BehaviorType;
import randoop.main.GenTests;
import randoop.main.OptionsCache;
import randoop.main.ThrowClassNameError;
import randoop.operation.TypedOperation;
import randoop.reflection.AccessibilityPredicate;
import randoop.reflection.DefaultReflectionPredicate;
import randoop.reflection.OmitMethodsPredicate;
import randoop.reflection.OperationModel;
import randoop.reflection.ReflectionPredicate;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Sequence;
import randoop.test.Check;
import randoop.test.ContractSet;
import randoop.test.EmptyExceptionCheck;
import randoop.test.ExceptionCheck;
import randoop.test.ExpectedExceptionCheck;
import randoop.test.NoExceptionCheck;
import randoop.test.TestCheckGenerator;
import randoop.test.TestChecks;
import randoop.types.JavaTypes;
import randoop.util.MultiMap;
import randoop.util.ReflectionExecutor;
import randoop.util.SimpleList;
import randoop.util.predicate.AlwaysTrue;

/**
 * Tests the classification of tests based on exception behavior assignments. So, question is where
 * exceptions are placed.
 */
public class TestClassificationTest {

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
   * Tests the classification of tests when all exceptions are invalid. Because of class will have
   * no error tests, and regression tests should have no exceptions.
   */
  @Test
  public void allInvalidTest() {
    randoop.util.Randomness.setSeed(0);
    ReflectionExecutor.resetStatistics();

    GenInputsAbstract.require_classname_in_test = null;
    GenInputsAbstract.no_regression_assertions = false;
    GenInputsAbstract.checked_exception = BehaviorType.INVALID;
    GenInputsAbstract.unchecked_exception = BehaviorType.INVALID;
    GenInputsAbstract.npe_on_null_input = BehaviorType.INVALID;
    GenInputsAbstract.npe_on_non_null_input = BehaviorType.INVALID;
    GenInputsAbstract.cm_exception = BehaviorType.INVALID;
    GenInputsAbstract.oom_exception = BehaviorType.INVALID;
    GenInputsAbstract.sof_exception = BehaviorType.INVALID;
    GenInputsAbstract.output_limit = 1000;
    GenInputsAbstract.forbid_null = false;

    Class<?> c = Flaky.class;
    ForwardGenerator gen = buildGenerator(c);
    gen.createAndClassifySequences();
    List<ExecutableSequence> rTests = gen.getRegressionSequences();
    List<ExecutableSequence> eTests = gen.getErrorTestSequences();

    assertFalse(rTests.isEmpty());

    for (ExecutableSequence eseq : rTests) {
      TestChecks<?> cks = eseq.getChecks();
      if (!cks.hasChecks()) {
        assertFalse(cks.hasErrorBehavior());
        assertFalse(cks.hasInvalidBehavior());
      }
      ExceptionCheck eck = cks.getExceptionCheck();
      if (eck != null) {
        fail(
            String.format(
                "all exceptions are invalid, regression checks should be null;%n have %s with %s"
                    + eck.getClass().getName()
                    + eck.getExceptionName()));
      }
    }

    assertEquals(0, eTests.size());
  }

  /**
   * Tests the classification of tests when all exceptions are errors. All exceptions should show as
   * NoExceptionCheck, and should be no expected exceptions in regression tests.
   */
  @Test
  public void allErrorTest() {
    randoop.util.Randomness.setSeed(0);
    ReflectionExecutor.resetStatistics();

    GenInputsAbstract.require_classname_in_test = null;
    GenInputsAbstract.no_regression_assertions = false;
    GenInputsAbstract.checked_exception = BehaviorType.ERROR;
    GenInputsAbstract.unchecked_exception = BehaviorType.ERROR;
    GenInputsAbstract.npe_on_null_input = BehaviorType.ERROR;
    GenInputsAbstract.npe_on_non_null_input = BehaviorType.ERROR;
    GenInputsAbstract.cm_exception = BehaviorType.ERROR;
    GenInputsAbstract.oom_exception = BehaviorType.ERROR;
    GenInputsAbstract.sof_exception = BehaviorType.ERROR;
    GenInputsAbstract.output_limit = 1000;
    GenInputsAbstract.forbid_null = false;

    Class<?> c = Flaky.class;
    ForwardGenerator gen = buildGenerator(c);
    gen.createAndClassifySequences();
    List<ExecutableSequence> rTests = gen.getRegressionSequences();
    List<ExecutableSequence> eTests = gen.getErrorTestSequences();

    assertFalse(rTests.isEmpty());

    for (ExecutableSequence eseq : rTests) {
      TestChecks<?> cks = eseq.getChecks();
      assertFalse(cks.hasErrorBehavior());
      assertFalse(cks.hasInvalidBehavior());

      ExceptionCheck eck = cks.getExceptionCheck();
      if (eck != null) {
        fail(
            String.format(
                "all exceptions error, should have no expected;%n have %s with %s",
                eck.getClass().getName(), eck.getExceptionName()));
      }
    }

    assertFalse(eTests.isEmpty());

    for (ExecutableSequence eseq : eTests) {
      TestChecks<?> cks = eseq.getChecks();
      assertTrue(cks.hasChecks());
      assertTrue(cks.hasErrorBehavior());
      assertFalse(cks.hasInvalidBehavior());

      int exceptionCount = 0;
      for (Check ck : cks.checks()) {
        if (ck instanceof NoExceptionCheck) {
          exceptionCount++;
        }
      }
      assertEquals(1, exceptionCount);
    }
  }

  /**
   * Tests classification of tests when all exceptions are expected. All exceptions should show as
   * expected exception checks, and there should be no error tests.
   */
  @Test
  public void allExpectedTest() {
    randoop.util.Randomness.setSeed(0);
    ReflectionExecutor.resetStatistics();

    GenInputsAbstract.require_classname_in_test = null;
    GenInputsAbstract.no_regression_assertions = false;
    GenInputsAbstract.checked_exception = BehaviorType.EXPECTED;
    GenInputsAbstract.unchecked_exception = BehaviorType.EXPECTED;
    GenInputsAbstract.npe_on_null_input = BehaviorType.EXPECTED;
    GenInputsAbstract.npe_on_non_null_input = BehaviorType.EXPECTED;
    GenInputsAbstract.cm_exception = BehaviorType.EXPECTED;
    GenInputsAbstract.oom_exception = BehaviorType.EXPECTED;
    GenInputsAbstract.sof_exception = BehaviorType.EXPECTED;
    GenInputsAbstract.output_limit = 1000;
    GenInputsAbstract.forbid_null = false;

    Class<?> c = Flaky.class;
    ForwardGenerator gen = buildGenerator(c);
    gen.createAndClassifySequences();
    List<ExecutableSequence> rTests = gen.getRegressionSequences();
    List<ExecutableSequence> eTests = gen.getErrorTestSequences();

    assertFalse(rTests.isEmpty());

    for (ExecutableSequence eseq : rTests) {
      TestChecks<?> cks = eseq.getChecks();
      assertFalse(cks.hasErrorBehavior());
      assertFalse(cks.hasInvalidBehavior());

      ExceptionCheck eck = cks.getExceptionCheck();
      if (eck != null) {
        assertTrue(cks.hasChecks());
        assertTrue(
            "should be expected exception, was" + eck.getClass().getName(),
            eck instanceof ExpectedExceptionCheck);
      }
    }

    assertEquals(0, eTests.size());
  }

  /**
   * Tests classification of tests when behavior type defaults are set (checked and unchecked
   * exceptions are expected, and both NPE-on-null and OOM are invalid). Because class throws NPE
   * without input, should see NPE as expected when no null inputs. Otherwise, should not see NPE.
   */
  @Test
  public void defaultsTest() {
    randoop.util.Randomness.setSeed(0);
    ReflectionExecutor.resetStatistics();

    GenInputsAbstract.require_classname_in_test = null;
    GenInputsAbstract.no_regression_assertions = false;
    GenInputsAbstract.checked_exception = BehaviorType.EXPECTED;
    GenInputsAbstract.unchecked_exception = BehaviorType.EXPECTED;
    GenInputsAbstract.npe_on_null_input = BehaviorType.EXPECTED;
    GenInputsAbstract.npe_on_non_null_input = BehaviorType.ERROR;
    GenInputsAbstract.cm_exception = BehaviorType.INVALID;
    GenInputsAbstract.oom_exception = BehaviorType.INVALID;
    GenInputsAbstract.sof_exception = BehaviorType.INVALID;
    GenInputsAbstract.output_limit = 1000;
    GenInputsAbstract.forbid_null = false;

    Class<?> c = Flaky.class;
    ForwardGenerator gen = buildGenerator(c);
    gen.createAndClassifySequences();
    List<ExecutableSequence> rTests = gen.getRegressionSequences();
    List<ExecutableSequence> eTests = gen.getErrorTestSequences();

    assertFalse(rTests.isEmpty());

    for (ExecutableSequence eseq : rTests) {
      TestChecks<?> cks = eseq.getChecks();
      assertFalse(cks.hasErrorBehavior());
      assertFalse(cks.hasInvalidBehavior());

      ExceptionCheck eck = cks.getExceptionCheck();
      if (eck != null) {
        assertTrue(cks.hasChecks());
        assertTrue(
            "should be expected exception, was" + eck.getClass().getName(),
            eck instanceof ExpectedExceptionCheck);
      }
    }

    assertFalse(eTests.isEmpty());

    for (ExecutableSequence eseq : eTests) {
      TestChecks<?> cks = eseq.getChecks();
      assertTrue(cks.hasChecks());
      assertTrue(cks.hasErrorBehavior());
      assertFalse(cks.hasInvalidBehavior());

      int exceptionCount = 0;
      for (Check ck : cks.checks()) {
        if (ck instanceof NoExceptionCheck) {
          exceptionCount++;
        }
      }
      assertEquals(1, exceptionCount);
    }
  }

  /**
   * Tests default behaviors with regression assertions turned off. Means that because class throws
   * NPE without input, should see NPE as empty exception when there are no null inputs. Otherwise,
   * should not see NPE, or any other checks.
   */
  @Test
  public void defaultsWithNoRegressionAssertions() {
    randoop.util.Randomness.setSeed(0);
    ReflectionExecutor.resetStatistics();

    GenInputsAbstract.require_classname_in_test = null;
    GenInputsAbstract.no_regression_assertions = true;
    GenInputsAbstract.checked_exception = BehaviorType.EXPECTED;
    GenInputsAbstract.unchecked_exception = BehaviorType.EXPECTED;
    GenInputsAbstract.npe_on_null_input = BehaviorType.EXPECTED;
    GenInputsAbstract.npe_on_non_null_input = BehaviorType.ERROR;
    GenInputsAbstract.cm_exception = BehaviorType.INVALID;
    GenInputsAbstract.oom_exception = BehaviorType.INVALID;
    GenInputsAbstract.sof_exception = BehaviorType.INVALID;
    GenInputsAbstract.output_limit = 1000;
    GenInputsAbstract.forbid_null = false;

    Class<?> c = Flaky.class;
    ForwardGenerator gen = buildGenerator(c);
    gen.createAndClassifySequences();
    List<ExecutableSequence> rTests = gen.getRegressionSequences();
    List<ExecutableSequence> eTests = gen.getErrorTestSequences();

    assertFalse(rTests.isEmpty());

    for (ExecutableSequence eseq : rTests) {
      TestChecks<?> cks = eseq.getChecks();
      assertFalse(cks.hasErrorBehavior());
      assertFalse(cks.hasInvalidBehavior());

      ExceptionCheck eck = cks.getExceptionCheck();
      if (eck != null) {
        assertTrue(cks.hasChecks());
        assertTrue(
            "should be expected exception, was" + eck.getClass().getName(),
            eck instanceof EmptyExceptionCheck);
      } else {
        assertFalse(cks.hasChecks());
      }
    }

    assertFalse(eTests.isEmpty());

    for (ExecutableSequence eseq : eTests) {
      TestChecks<?> cks = eseq.getChecks();
      assertTrue(cks.hasChecks());
      assertTrue(cks.hasErrorBehavior());
      assertFalse(cks.hasInvalidBehavior());

      int exceptionCount = 0;
      for (Check ck : cks.checks()) {
        if (ck instanceof NoExceptionCheck) {
          exceptionCount++;
        }
      }
      assertEquals(1, exceptionCount);
    }
  }

  /**
   * The tests generated here should throw an ArrayStoreException, which is a RuntimeException. Want
   * to make that resulting sequence is not going into component manager.
   */
  @Test
  public void regressionTestGeneration() {
    randoop.util.Randomness.setSeed(0);
    ReflectionExecutor.resetStatistics();

    GenInputsAbstract.unchecked_exception = BehaviorType.EXPECTED;
    GenInputsAbstract.generated_limit = 100;
    Class<?> c = FlakyStore.class;
    ComponentManager componentManager = getComponentManager();
    AccessibilityPredicate accessibility = IS_PUBLIC;
    TestCheckGenerator checkGenerator =
        GenTests.createTestCheckGenerator(
            accessibility, new ContractSet(), new MultiMap<>(), OmitMethodsPredicate.NO_OMISSION);
    ForwardGenerator gen = buildGenerator(c, componentManager, accessibility, checkGenerator);
    gen.createAndClassifySequences();
    List<ExecutableSequence> rTests = gen.getRegressionSequences();
    List<ExecutableSequence> eTests = gen.getErrorTestSequences();
    assertEquals(0, eTests.size());

    SimpleList<Sequence> sequences = componentManager.getSequencesForType(JavaTypes.BOOLEAN_TYPE);
    for (ExecutableSequence es : rTests) {
      if (!es.isNormalExecution()) {
        int exceptionIndex = es.getNonNormalExecutionIndex();
        ExecutionOutcome outcome = es.getResult(exceptionIndex);
        assertTrue(outcome instanceof ExceptionalExecution);
        Throwable exception = ((ExceptionalExecution) outcome).getException();
        assertTrue(exception instanceof ArrayStoreException);

        for (int i = 0; i < sequences.size(); i++) {
          assertFalse(es.sequence.equals(sequences.get(i)));
        }
      }
    }
  }

  private ForwardGenerator buildGenerator(
      Class<?> c,
      ComponentManager componentMgr,
      AccessibilityPredicate accessibility,
      TestCheckGenerator checkGenerator) {
    Set<@ClassGetName String> classnames = new HashSet<>();
    classnames.add(c.getName());
    Set<String> omitfields = new HashSet<>();

    ReflectionPredicate reflectionPredicate = new DefaultReflectionPredicate(omitfields);
    OperationModel operationModel = null;
    try {
      operationModel =
          OperationModel.createModel(
              accessibility,
              reflectionPredicate,
              GenInputsAbstract.omit_methods,
              classnames,
              new HashSet<>(),
              new ThrowClassNameError(),
              new ArrayList<String>());
    } catch (Exception e) {
      fail("couldn't build model " + e.getMessage());
    }
    final List<TypedOperation> model = operationModel.getOperations();

    RandoopListenerManager listenerMgr = new RandoopListenerManager();
    ForwardGenerator gen =
        new ForwardGenerator(
            model,
            new LinkedHashSet<TypedOperation>(),
            new GenInputsAbstract.Limits(),
            componentMgr,
            null,
            listenerMgr,
            operationModel.getClassTypes());
    Predicate<ExecutableSequence> isOutputTest = new AlwaysTrue<>();
    gen.setTestPredicate(isOutputTest);

    gen.setTestCheckGenerator(checkGenerator);
    gen.setExecutionVisitor(new DummyVisitor());
    return gen;
  }

  private ForwardGenerator buildGenerator(Class<?> c) {
    ComponentManager componentMgr = getComponentManager();
    AccessibilityPredicate accessibility = IS_PUBLIC;
    TestCheckGenerator checkGenerator =
        GenTests.createTestCheckGenerator(
            accessibility, new ContractSet(), new MultiMap<>(), OmitMethodsPredicate.NO_OMISSION);
    return buildGenerator(c, componentMgr, accessibility, checkGenerator);
  }

  private ComponentManager getComponentManager() {
    Collection<Sequence> components = new LinkedHashSet<>();
    components.addAll(SeedSequences.defaultSeeds());
    return new ComponentManager(components);
  }
}
