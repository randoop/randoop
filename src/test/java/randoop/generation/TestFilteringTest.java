package randoop.generation;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import randoop.DummyVisitor;
import randoop.contract.ObjectContract;
import randoop.main.GenInputsAbstract;
import randoop.main.GenInputsAbstract.BehaviorType;
import randoop.main.GenTests;
import randoop.operation.ConcreteOperation;
import randoop.reflection.DefaultReflectionPredicate;
import randoop.reflection.ModelCollections;
import randoop.reflection.OperationExtractor;
import randoop.reflection.PublicVisibilityPredicate;
import randoop.reflection.ReflectionManager;
import randoop.reflection.ReflectionPredicate;
import randoop.reflection.TypedOperationManager;
import randoop.reflection.VisibilityPredicate;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Sequence;
import randoop.test.TestCheckGenerator;
import randoop.types.ConcreteType;
import randoop.util.MultiMap;
import randoop.util.predicate.Predicate;

import static org.junit.Assert.assertTrue;

public class TestFilteringTest {

  /**
   * Make sure that we are getting both regression and error tests with
   * default filtering settings.
   */
  @Test
  public void nonemptyOutputTest() {
    GenInputsAbstract.dont_output_tests = false;
    GenInputsAbstract.include_if_classname_appears = null;
    GenInputsAbstract.no_error_revealing_tests = false;
    GenInputsAbstract.no_regression_tests = false;
    // arguments below ensure we get both kinds of tests
    GenInputsAbstract.no_regression_assertions = false;
    GenInputsAbstract.checked_exception = BehaviorType.EXPECTED;
    GenInputsAbstract.unchecked_exception = BehaviorType.ERROR;
    GenInputsAbstract.npe_on_null_input = BehaviorType.ERROR;
    GenInputsAbstract.npe_on_non_null_input = BehaviorType.ERROR;
    GenInputsAbstract.oom_exception = BehaviorType.INVALID;
    GenInputsAbstract.outputlimit = 1000;
    GenInputsAbstract.forbid_null = false;

    Class<?> c = Flaky.class;
    ForwardGenerator gen = buildGenerator(c);
    gen.explore();
    List<ExecutableSequence> rTests = gen.getRegressionSequences();
    List<ExecutableSequence> eTests = gen.getErrorTestSequences();

    assertTrue("should have some regression tests", rTests.size() > 0);
    assertTrue("should have some error tests", eTests.size() > 0);
  }

  /**
   * Make sure there is no output when dont-output-tests is set.
   * Need to set an input limit here.
   */
  @Test
  public void noOutputTest() {
    GenInputsAbstract.dont_output_tests = true;
    GenInputsAbstract.include_if_classname_appears = null;
    GenInputsAbstract.no_error_revealing_tests = false;
    GenInputsAbstract.no_regression_tests = false;
    // arguments below ensure we get both kinds of tests
    GenInputsAbstract.no_regression_assertions = false;
    GenInputsAbstract.checked_exception = BehaviorType.EXPECTED;
    GenInputsAbstract.unchecked_exception = BehaviorType.ERROR;
    GenInputsAbstract.npe_on_null_input = BehaviorType.ERROR;
    GenInputsAbstract.npe_on_non_null_input = BehaviorType.ERROR;
    GenInputsAbstract.oom_exception = BehaviorType.INVALID;
    GenInputsAbstract.outputlimit = 1000;
    GenInputsAbstract.inputlimit = 1000;
    GenInputsAbstract.forbid_null = false;

    Class<?> c = Flaky.class;
    ForwardGenerator gen = buildGenerator(c);
    gen.explore();
    List<ExecutableSequence> rTests = gen.getRegressionSequences();
    List<ExecutableSequence> eTests = gen.getErrorTestSequences();

    assertTrue("should have no regression tests", rTests.size() == 0);
    assertTrue("should have no error tests", eTests.size() == 0);
  }

  /**
   * Make sure get no error test output when no-error-revealing-tests is set.
   */
  @Test
  public void noErrorOutputTest() {
    GenInputsAbstract.dont_output_tests = false;
    GenInputsAbstract.include_if_classname_appears = null;
    GenInputsAbstract.no_error_revealing_tests = true;
    GenInputsAbstract.no_regression_tests = false;
    // arguments below ensure we get both kinds of tests
    GenInputsAbstract.no_regression_assertions = false;
    GenInputsAbstract.checked_exception = BehaviorType.EXPECTED;
    GenInputsAbstract.unchecked_exception = BehaviorType.ERROR;
    GenInputsAbstract.npe_on_null_input = BehaviorType.ERROR;
    GenInputsAbstract.npe_on_non_null_input = BehaviorType.ERROR;
    GenInputsAbstract.oom_exception = BehaviorType.INVALID;
    GenInputsAbstract.outputlimit = 1000;
    GenInputsAbstract.forbid_null = false;

    Class<?> c = Flaky.class;
    ForwardGenerator gen = buildGenerator(c);
    gen.explore();
    List<ExecutableSequence> rTests = gen.getRegressionSequences();
    List<ExecutableSequence> eTests = gen.getErrorTestSequences();

    assertTrue("should have some regression tests", rTests.size() > 0);
    assertTrue("should have no error tests", eTests.size() == 0);
  }

  /**
   * Make sure that no regression tests are output when no-regression-tests is set.
   * Better to set inputlimit here since most tests are regression tests.
   */
  @Test
  public void noRegressionOutputTest() {
    GenInputsAbstract.dont_output_tests = false;
    GenInputsAbstract.include_if_classname_appears = null;
    GenInputsAbstract.no_error_revealing_tests = false;
    GenInputsAbstract.no_regression_tests = true;
    // arguments below ensure we get both kinds of tests
    GenInputsAbstract.no_regression_assertions = false;
    GenInputsAbstract.checked_exception = BehaviorType.EXPECTED;
    GenInputsAbstract.unchecked_exception = BehaviorType.ERROR;
    GenInputsAbstract.npe_on_null_input = BehaviorType.ERROR;
    GenInputsAbstract.npe_on_non_null_input = BehaviorType.ERROR;
    GenInputsAbstract.oom_exception = BehaviorType.INVALID;
    GenInputsAbstract.outputlimit = 1000;
    GenInputsAbstract.inputlimit = 1000;
    GenInputsAbstract.forbid_null = false;

    Class<?> c = Flaky.class;
    ForwardGenerator gen = buildGenerator(c);
    gen.explore();
    List<ExecutableSequence> rTests = gen.getRegressionSequences();
    List<ExecutableSequence> eTests = gen.getErrorTestSequences();

    assertTrue("should have no regression tests, but getting " + rTests.size(), rTests.size() == 0);
    assertTrue("should have some error tests", eTests.size() > 0);
  }

  /**
   * Having both Error and Regression tests turned off should give nothing.
   * Set inputlimit
   */
  @Test
  public void noErrorOrRegressionOutputTest() {
    GenInputsAbstract.dont_output_tests = false;
    GenInputsAbstract.include_if_classname_appears = null;
    GenInputsAbstract.no_error_revealing_tests = true;
    GenInputsAbstract.no_regression_tests = true;
    // arguments below ensure we get both kinds of tests
    GenInputsAbstract.no_regression_assertions = false;
    GenInputsAbstract.checked_exception = BehaviorType.EXPECTED;
    GenInputsAbstract.unchecked_exception = BehaviorType.ERROR;
    GenInputsAbstract.npe_on_null_input = BehaviorType.ERROR;
    GenInputsAbstract.npe_on_non_null_input = BehaviorType.ERROR;
    GenInputsAbstract.oom_exception = BehaviorType.INVALID;
    GenInputsAbstract.outputlimit = 1000;
    GenInputsAbstract.inputlimit = 1000;
    GenInputsAbstract.forbid_null = false;

    Class<?> c = Flaky.class;
    ForwardGenerator gen = buildGenerator(c);
    gen.explore();
    List<ExecutableSequence> rTests = gen.getRegressionSequences();
    List<ExecutableSequence> eTests = gen.getErrorTestSequences();

    assertTrue("should have no regression tests", rTests.size() == 0);
    assertTrue("should have no error tests", eTests.size() == 0);
  }

  /**
   * Filtering tests matching CUT should produce output tests.
   */
  @Test
  public void matchOutputTest() {
    GenInputsAbstract.dont_output_tests = false;
    GenInputsAbstract.include_if_classname_appears = Pattern.compile("randoop\\.sequence\\.Flaky");
    GenInputsAbstract.no_error_revealing_tests = false;
    GenInputsAbstract.no_regression_tests = false;
    // arguments below ensure we get both kinds of tests
    GenInputsAbstract.no_regression_assertions = false;
    GenInputsAbstract.checked_exception = BehaviorType.EXPECTED;
    GenInputsAbstract.unchecked_exception = BehaviorType.ERROR;
    GenInputsAbstract.npe_on_null_input = BehaviorType.ERROR;
    GenInputsAbstract.npe_on_non_null_input = BehaviorType.ERROR;
    GenInputsAbstract.oom_exception = BehaviorType.INVALID;
    GenInputsAbstract.outputlimit = 1000;
    GenInputsAbstract.inputlimit = 1000;
    GenInputsAbstract.forbid_null = false;

    Class<?> c = Flaky.class;
    ForwardGenerator gen = buildGenerator(c);
    gen.explore();
    List<ExecutableSequence> rTests = gen.getRegressionSequences();
    List<ExecutableSequence> eTests = gen.getErrorTestSequences();

    assertTrue("should have some regression tests", rTests.size() > 0);
    assertTrue("should have some error tests", eTests.size() > 0);
  }

  private ForwardGenerator buildGenerator(Class<?> c) {

    Set<String> omitfields = new HashSet<>();
    VisibilityPredicate visibility = new PublicVisibilityPredicate();
    ReflectionPredicate predicate =
            new DefaultReflectionPredicate(GenInputsAbstract.omitmethods, omitfields);
    final List<ConcreteOperation> model = new ArrayList<>();
    TypedOperationManager operationManager = new TypedOperationManager(new ModelCollections() {
      @Override
      public void addConcreteOperation(ConcreteType declaringType, ConcreteOperation operation) {
        model.add(operation);
      }
    });
    ReflectionManager manager = new ReflectionManager(visibility);
    manager.add(new OperationExtractor(operationManager, predicate));
    manager.apply(c);
    Collection<Sequence> components = new LinkedHashSet<>();
    components.addAll(SeedSequences.defaultSeeds());
    ComponentManager componentMgr = new ComponentManager(components);
    RandoopListenerManager listenerMgr = new RandoopListenerManager();
    ForwardGenerator gen =
            new ForwardGenerator(
                    model,
                    new LinkedHashSet<ConcreteOperation>(),
                    GenInputsAbstract.timelimit * 1000,
                    GenInputsAbstract.inputlimit,
                    GenInputsAbstract.outputlimit,
                    componentMgr,
                    null,
                    listenerMgr);
    GenTests genTests = new GenTests();
    Predicate<ExecutableSequence> isOutputTest = genTests.createTestOutputPredicate(new HashSet<Sequence>(), new HashSet<Class<?>>(), null);
    gen.addTestPredicate(isOutputTest);
    TestCheckGenerator checkGenerator =
            (new GenTests()).createTestCheckGenerator(visibility, new LinkedHashSet<ObjectContract>(), new MultiMap<ConcreteType, ConcreteOperation>(), new LinkedHashSet<ConcreteOperation>());
    gen.addTestCheckGenerator(checkGenerator);
    gen.addExecutionVisitor(new DummyVisitor());
    return gen;
  }
}
