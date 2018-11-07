package randoop.generation;

import static org.junit.Assert.assertTrue;
import static randoop.reflection.VisibilityPredicate.IS_PUBLIC;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import randoop.DummyVisitor;
import randoop.main.GenInputsAbstract;
import randoop.main.GenInputsAbstract.BehaviorType;
import randoop.main.GenTests;
import randoop.main.OptionsCache;
import randoop.operation.TypedOperation;
import randoop.reflection.DefaultReflectionPredicate;
import randoop.reflection.OmitMethodsPredicate;
import randoop.reflection.OperationExtractor;
import randoop.reflection.ReflectionManager;
import randoop.reflection.ReflectionPredicate;
import randoop.reflection.VisibilityPredicate;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Sequence;
import randoop.test.ContractSet;
import randoop.test.TestCheckGenerator;
import randoop.types.ClassOrInterfaceType;
import randoop.types.Type;
import randoop.util.MultiMap;
import randoop.util.ReflectionExecutor;

public class TestFilteringTest {

  private static OptionsCache optionsCache;

  @BeforeClass
  public static void setup() {
    optionsCache = new OptionsCache();
    optionsCache.saveState();

    TestUtils.setRandoopLog();
    TestUtils.setSelectionLog();
  }

  @AfterClass
  public static void restore() {
    optionsCache.restoreState();
  }

  /**
   * Make sure that we are getting both regression and error tests with default filtering settings.
   */
  @Test
  public void nonemptyOutputTest() {
    randoop.util.Randomness.setSeed(0);
    ReflectionExecutor.resetStatistics();

    GenInputsAbstract.dont_output_tests = false;
    GenInputsAbstract.require_classname_in_test = null;
    GenInputsAbstract.no_error_revealing_tests = false;
    GenInputsAbstract.no_regression_tests = false;
    // arguments below ensure we get both kinds of tests
    GenInputsAbstract.no_regression_assertions = false;
    GenInputsAbstract.checked_exception = BehaviorType.EXPECTED;
    GenInputsAbstract.unchecked_exception = BehaviorType.ERROR;
    GenInputsAbstract.npe_on_null_input = BehaviorType.ERROR;
    GenInputsAbstract.npe_on_non_null_input = BehaviorType.ERROR;
    GenInputsAbstract.oom_exception = BehaviorType.INVALID;
    GenInputsAbstract.sof_exception = BehaviorType.INVALID;
    GenInputsAbstract.output_limit = 1000;
    GenInputsAbstract.forbid_null = false;

    Class<?> c = Flaky.class;
    ForwardGenerator gen = buildAndRunGenerator(c);
    List<ExecutableSequence> rTests = gen.getRegressionSequences();
    List<ExecutableSequence> eTests = gen.getErrorTestSequences();

    assertTrue("should have some regression tests", rTests.size() > 0);
    assertTrue("should have some error tests", eTests.size() > 0);
  }

  /**
   * Make sure there is no output when dont-output-tests is set. Need to set an input limit here.
   */
  @Test
  public void noOutputTest() {
    randoop.util.Randomness.setSeed(0);
    ReflectionExecutor.resetStatistics();

    GenInputsAbstract.dont_output_tests = true;
    GenInputsAbstract.require_classname_in_test = null;
    GenInputsAbstract.no_error_revealing_tests = false;
    GenInputsAbstract.no_regression_tests = false;
    // arguments below ensure we get both kinds of tests
    GenInputsAbstract.no_regression_assertions = false;
    GenInputsAbstract.checked_exception = BehaviorType.EXPECTED;
    GenInputsAbstract.unchecked_exception = BehaviorType.ERROR;
    GenInputsAbstract.npe_on_null_input = BehaviorType.ERROR;
    GenInputsAbstract.npe_on_non_null_input = BehaviorType.ERROR;
    GenInputsAbstract.oom_exception = BehaviorType.INVALID;
    GenInputsAbstract.sof_exception = BehaviorType.INVALID;
    GenInputsAbstract.generated_limit = 1000;
    GenInputsAbstract.output_limit = 1000;
    GenInputsAbstract.forbid_null = false;

    Class<?> c = Flaky.class;
    ForwardGenerator gen = buildAndRunGenerator(c);
    List<ExecutableSequence> rTests = gen.getRegressionSequences();
    List<ExecutableSequence> eTests = gen.getErrorTestSequences();

    assertTrue("should have no regression tests", rTests.size() == 0);
    assertTrue("should have no error tests", eTests.size() == 0);
  }

  /** Make sure get no error test output when no-error-revealing-tests is set. */
  @Test
  public void noErrorOutputTest() {
    randoop.util.Randomness.setSeed(0);
    ReflectionExecutor.resetStatistics();

    GenInputsAbstract.dont_output_tests = false;
    GenInputsAbstract.require_classname_in_test = null;
    GenInputsAbstract.no_error_revealing_tests = true;
    GenInputsAbstract.no_regression_tests = false;
    // arguments below ensure we get both kinds of tests
    GenInputsAbstract.no_regression_assertions = false;
    GenInputsAbstract.checked_exception = BehaviorType.EXPECTED;
    GenInputsAbstract.unchecked_exception = BehaviorType.ERROR;
    GenInputsAbstract.npe_on_null_input = BehaviorType.ERROR;
    GenInputsAbstract.npe_on_non_null_input = BehaviorType.ERROR;
    GenInputsAbstract.oom_exception = BehaviorType.INVALID;
    GenInputsAbstract.sof_exception = BehaviorType.INVALID;
    GenInputsAbstract.output_limit = 1000;
    GenInputsAbstract.forbid_null = false;

    Class<?> c = Flaky.class;
    ForwardGenerator gen = buildAndRunGenerator(c);
    List<ExecutableSequence> rTests = gen.getRegressionSequences();
    List<ExecutableSequence> eTests = gen.getErrorTestSequences();

    assertTrue("should have some regression tests", rTests.size() > 0);
    assertTrue("should have no error tests", eTests.size() == 0);
  }

  /**
   * Make sure that no regression tests are output when no-regression-tests is set. Better to set
   * generated_limit here since most tests are regression tests.
   */
  @Test
  public void noRegressionOutputTest() {
    randoop.util.Randomness.setSeed(0);
    ReflectionExecutor.resetStatistics();

    GenInputsAbstract.dont_output_tests = false;
    GenInputsAbstract.require_classname_in_test = null;
    GenInputsAbstract.no_error_revealing_tests = false;
    GenInputsAbstract.no_regression_tests = true;
    // arguments below ensure we get both kinds of tests
    GenInputsAbstract.no_regression_assertions = false;
    GenInputsAbstract.checked_exception = BehaviorType.EXPECTED;
    GenInputsAbstract.unchecked_exception = BehaviorType.ERROR;
    GenInputsAbstract.npe_on_null_input = BehaviorType.ERROR;
    GenInputsAbstract.npe_on_non_null_input = BehaviorType.ERROR;
    GenInputsAbstract.oom_exception = BehaviorType.INVALID;
    GenInputsAbstract.sof_exception = BehaviorType.INVALID;
    GenInputsAbstract.generated_limit = 1000;
    GenInputsAbstract.output_limit = 1000;
    GenInputsAbstract.forbid_null = false;

    Class<?> c = Flaky.class;
    ForwardGenerator gen = buildAndRunGenerator(c);
    List<ExecutableSequence> rTests = gen.getRegressionSequences();
    List<ExecutableSequence> eTests = gen.getErrorTestSequences();

    assertTrue("should have no regression tests, but getting " + rTests.size(), rTests.size() == 0);
    assertTrue("should have some error tests", eTests.size() > 0);
  }

  /** Having both Error and Regression tests turned off should give nothing. Set generated_limit. */
  @Test
  public void noErrorOrRegressionOutputTest() {
    randoop.util.Randomness.setSeed(0);
    ReflectionExecutor.resetStatistics();

    GenInputsAbstract.dont_output_tests = false;
    GenInputsAbstract.require_classname_in_test = null;
    GenInputsAbstract.no_error_revealing_tests = true;
    GenInputsAbstract.no_regression_tests = true;
    // arguments below ensure we get both kinds of tests
    GenInputsAbstract.no_regression_assertions = false;
    GenInputsAbstract.checked_exception = BehaviorType.EXPECTED;
    GenInputsAbstract.unchecked_exception = BehaviorType.ERROR;
    GenInputsAbstract.npe_on_null_input = BehaviorType.ERROR;
    GenInputsAbstract.npe_on_non_null_input = BehaviorType.ERROR;
    GenInputsAbstract.oom_exception = BehaviorType.INVALID;
    GenInputsAbstract.sof_exception = BehaviorType.INVALID;
    GenInputsAbstract.generated_limit = 1000;
    GenInputsAbstract.output_limit = 1000;
    GenInputsAbstract.forbid_null = false;

    Class<?> c = Flaky.class;
    ForwardGenerator gen = buildAndRunGenerator(c);
    List<ExecutableSequence> rTests = gen.getRegressionSequences();
    List<ExecutableSequence> eTests = gen.getErrorTestSequences();

    assertTrue("should have no regression tests", rTests.size() == 0);
    assertTrue("should have no error tests", eTests.size() == 0);
  }

  /** Filtering tests matching CUT should produce output tests. */
  @Test
  public void matchOutputTest() {
    randoop.util.Randomness.setSeed(0);
    ReflectionExecutor.resetStatistics();

    GenInputsAbstract.dont_output_tests = false;
    GenInputsAbstract.require_classname_in_test = Pattern.compile("randoop\\.sequence\\.Flaky");
    GenInputsAbstract.no_error_revealing_tests = false;
    GenInputsAbstract.no_regression_tests = false;
    // arguments below ensure we get both kinds of tests
    GenInputsAbstract.no_regression_assertions = false;
    GenInputsAbstract.checked_exception = BehaviorType.EXPECTED;
    GenInputsAbstract.unchecked_exception = BehaviorType.ERROR;
    GenInputsAbstract.npe_on_null_input = BehaviorType.ERROR;
    GenInputsAbstract.npe_on_non_null_input = BehaviorType.ERROR;
    GenInputsAbstract.oom_exception = BehaviorType.INVALID;
    GenInputsAbstract.sof_exception = BehaviorType.INVALID;
    GenInputsAbstract.generated_limit = 1000;
    GenInputsAbstract.output_limit = 1000;
    GenInputsAbstract.forbid_null = false;

    Class<?> c = Flaky.class;
    ForwardGenerator gen = buildAndRunGenerator(c);
    List<ExecutableSequence> rTests = gen.getRegressionSequences();
    List<ExecutableSequence> eTests = gen.getErrorTestSequences();

    assertTrue("should have some regression tests", rTests.size() > 0);
    assertTrue("should have some error tests", eTests.size() > 0);
  }

  private ForwardGenerator buildAndRunGenerator(Class<?> c) {
    Set<String> omitfields = new HashSet<>();
    VisibilityPredicate visibility = IS_PUBLIC;
    ReflectionPredicate reflectionPredicate = new DefaultReflectionPredicate(omitfields);
    ClassOrInterfaceType classType = ClassOrInterfaceType.forClass(c);

    Set<ClassOrInterfaceType> classesUnderTest = new HashSet<>();
    classesUnderTest.add(classType);

    OmitMethodsPredicate omitMethodsPredicate =
        new OmitMethodsPredicate(GenInputsAbstract.omitmethods);
    ReflectionManager manager = new ReflectionManager(visibility);

    final OperationExtractor extractor =
        new OperationExtractor(classType, reflectionPredicate, omitMethodsPredicate, visibility);
    manager.apply(extractor, c);
    Collection<Sequence> components = new LinkedHashSet<>();
    components.addAll(SeedSequences.defaultSeeds());
    ComponentManager componentMgr = new ComponentManager(components);
    RandoopListenerManager listenerMgr = new RandoopListenerManager();
    ForwardGenerator gen =
        new ForwardGenerator(
            new ArrayList<>(extractor.getOperations()),
            new LinkedHashSet<TypedOperation>(),
            new GenInputsAbstract.Limits(),
            componentMgr,
            null,
            listenerMgr,
            classesUnderTest);
    GenTests genTests = new GenTests();
    Predicate<ExecutableSequence> isOutputTest =
        genTests.createTestOutputPredicate(new HashSet<Sequence>(), new HashSet<Class<?>>(), null);
    gen.setTestPredicate(isOutputTest);
    TestCheckGenerator checkGenerator =
        GenTests.createTestCheckGenerator(
            visibility, new ContractSet(), new MultiMap<Type, TypedOperation>());
    gen.setTestCheckGenerator(checkGenerator);
    gen.setExecutionVisitor(new DummyVisitor());
    TestUtils.setAllLogs(gen);
    gen.createAndClassifySequences();
    gen.getOperationHistory().outputTable();
    return gen;
  }
}
