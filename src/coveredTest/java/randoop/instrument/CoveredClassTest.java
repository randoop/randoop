package randoop.instrument;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static randoop.main.GenInputsAbstract.require_classname_in_test;
import static randoop.reflection.VisibilityPredicate.IS_PUBLIC;

import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import org.checkerframework.checker.signature.qual.ClassGetName;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import randoop.generation.ComponentManager;
import randoop.generation.ForwardGenerator;
import randoop.generation.RandoopListenerManager;
import randoop.generation.SeedSequences;
import randoop.generation.TestUtils;
import randoop.main.ClassNameErrorHandler;
import randoop.main.GenInputsAbstract;
import randoop.main.GenTests;
import randoop.main.OptionsCache;
import randoop.main.ThrowClassNameError;
import randoop.operation.TypedClassOperation;
import randoop.operation.TypedOperation;
import randoop.reflection.DefaultReflectionPredicate;
import randoop.reflection.OperationModel;
import randoop.reflection.ReflectionPredicate;
import randoop.reflection.SignatureParseException;
import randoop.reflection.TypeNames;
import randoop.reflection.VisibilityPredicate;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Sequence;
import randoop.test.ContractSet;
import randoop.test.TestCheckGenerator;
import randoop.types.Type;
import randoop.util.MultiMap;

public class CoveredClassTest {

  private static OptionsCache optionsCache;

  @BeforeClass
  public static void setup() {
    optionsCache = new OptionsCache();
    optionsCache.saveState();
    GenInputsAbstract.deterministic = true;
    GenInputsAbstract.minimize_error_test = false;
    GenInputsAbstract.time_limit = 0;
    GenInputsAbstract.generated_limit = 10000;
    GenInputsAbstract.output_limit = 5000;
    GenInputsAbstract.silently_ignore_bad_class_names = false;
  }

  @AfterClass
  public static void restore() {
    optionsCache.restoreState();
  }

  /**
   * Assert that no tests of a given type were generated. That is, fail if the given list is
   * non-empty.
   *
   * @param tests the list of tests, which should be empty
   * @param description the type of test; used in diagnostic messages
   */
  protected static void assertNoTests(List<ExecutableSequence> tests, String description) {
    if (!tests.isEmpty()) {
      System.out.println("number of " + description + " tests: " + tests.size());
      for (ExecutableSequence eseq : tests) {
        System.out.println();
        System.out.printf("%n%s%n", eseq);
      }
      fail("Didn't expect any " + description + " tests");
    }
  }

  @Test
  public void testNoFilter() {
    System.out.println("running testNoFilter");

    GenInputsAbstract.classlist = Paths.get("instrument/testcase/allclasses.txt");
    require_classname_in_test = null;
    GenInputsAbstract.require_covered_classes = null;
    // setup classes

    ForwardGenerator testGenerator = getGeneratorForTest();

    testGenerator.createAndClassifySequences();
    List<ExecutableSequence> rTests = testGenerator.getRegressionSequences();
    List<ExecutableSequence> eTests = testGenerator.getErrorTestSequences();

    System.out.println("number of regression tests: " + rTests.size());
    assertFalse(rTests.isEmpty());
    assertNoTests(eTests, "error");

    Class<?> ac;
    try {
      ac = TypeNames.getTypeForName("instrument.testcase.A");
    } catch (ClassNotFoundException e) {
      fail("cannot find class: " + e);
      throw new Error("dead code");
    }

    Class<?> cc;
    try {
      cc = TypeNames.getTypeForName("instrument.testcase.C");
    } catch (ClassNotFoundException e) {
      fail("cannot find class: " + e);
      throw new Error("dead code");
    }

    for (ExecutableSequence e : rTests) {
      assertFalse("should not cover the class: " + ac.getName(), e.coversClass(ac));
      assertFalse("should not cover the class: " + cc.getName(), e.coversClass(cc));
    }
  }

  @Test
  public void testNameFilter() {
    System.out.println("running testNameFilter");
    GenInputsAbstract.classlist = Paths.get("instrument/testcase/allclasses.txt");
    require_classname_in_test = Pattern.compile("instrument\\.testcase\\.A"); // null;
    GenInputsAbstract.require_covered_classes =
        null; // "tests/instrument/testcase/coveredclasses.txt";
    // setup classes

    ForwardGenerator testGenerator = getGeneratorForTest();

    testGenerator.createAndClassifySequences();
    List<ExecutableSequence> rTests = testGenerator.getRegressionSequences();
    List<ExecutableSequence> eTests = testGenerator.getErrorTestSequences();

    System.out.println("number of regression tests: " + rTests.size());
    assertNoTests(rTests, "regression");
    assertNoTests(eTests, "error");

    Class<?> ac;
    try {
      ac = TypeNames.getTypeForName("instrument.testcase.A");
    } catch (ClassNotFoundException e) {
      fail("cannot find class: " + e);
      throw new Error("dead code");
    }

    Class<?> cc;
    try {
      cc = TypeNames.getTypeForName("instrument.testcase.C");
    } catch (ClassNotFoundException e) {
      fail("cannot find class: " + e);
      throw new Error("dead code");
    }

    for (ExecutableSequence e : rTests) {
      assertFalse("should not cover the class: " + ac.getName(), e.coversClass(ac));
      assertFalse("should not cover the class: " + cc.getName(), e.coversClass(cc));
    }
  }

  @Test
  public void testCoverageFilter() {
    System.out.println("running testCoverageFilter");
    GenInputsAbstract.classlist = Paths.get("instrument/testcase/allclasses.txt");
    require_classname_in_test = null;
    GenInputsAbstract.require_covered_classes = Paths.get("instrument/testcase/coveredclasses.txt");
    // setup classes

    ForwardGenerator testGenerator = getGeneratorForTest();

    testGenerator.createAndClassifySequences();
    List<ExecutableSequence> rTests = testGenerator.getRegressionSequences();
    List<ExecutableSequence> eTests = testGenerator.getErrorTestSequences();

    System.out.println("number of regression tests: " + rTests.size());
    assertFalse(rTests.isEmpty());
    assertNoTests(eTests, "error");

    Class<?> ac;
    try {
      ac = TypeNames.getTypeForName("instrument.testcase.A");
    } catch (ClassNotFoundException e) {
      fail("cannot find class: " + e);
      throw new Error("dead code");
    }

    Class<?> cc;
    try {
      cc = TypeNames.getTypeForName("instrument.testcase.C");
    } catch (ClassNotFoundException e) {
      fail("cannot find class: " + e);
      throw new Error("dead code");
    }

    for (ExecutableSequence e : rTests) {
      assertTrue("should cover the class: " + ac.getName(), e.coversClass(ac));
      assertFalse("should not cover the class: " + cc.getName(), e.coversClass(cc));
    }
  }

  private ForwardGenerator getGeneratorForTest() {
    VisibilityPredicate visibility = IS_PUBLIC;
    Set<@ClassGetName String> classnames = GenInputsAbstract.getClassnamesFromArgs(visibility);
    Set<@ClassGetName String> coveredClassnames =
        GenInputsAbstract.getClassNamesFromFile(GenInputsAbstract.require_covered_classes);
    Set<String> omitFields =
        GenInputsAbstract.getStringSetFromFile(GenInputsAbstract.omit_field_file, "fields");
    ReflectionPredicate reflectionPredicate = new DefaultReflectionPredicate(omitFields);
    ClassNameErrorHandler classNameErrorHandler = new ThrowClassNameError();

    OperationModel operationModel;
    try {
      operationModel =
          OperationModel.createModel(
              visibility,
              reflectionPredicate,
              GenInputsAbstract.omit_methods,
              classnames,
              coveredClassnames,
              classNameErrorHandler,
              GenInputsAbstract.literals_file,
              null);
    } catch (SignatureParseException e) {
      fail("operation parse exception thrown: " + e);
      throw new Error("dead code");
    } catch (NoSuchMethodException e) {
      fail("Method not found: " + e);
      throw new Error("dead code");
    }
    assertNotNull(operationModel);

    List<TypedOperation> model = operationModel.getOperations();
    Set<Sequence> components = new LinkedHashSet<>();
    components.addAll(SeedSequences.defaultSeeds());
    components.addAll(operationModel.getAnnotatedTestValues());

    ComponentManager componentMgr = new ComponentManager(components);
    operationModel.addClassLiterals(
        componentMgr, GenInputsAbstract.literals_file, GenInputsAbstract.literals_level);

    // Maps each class type to the side-effect-free methods in it.
    MultiMap<Type, TypedClassOperation> sideEffectFreeMethodsByType =
        GenTests.readSideEffectFreeMethods();

    Set<TypedOperation> sideEffectFreeMethods = new LinkedHashSet<>();
    for (Type keyType : sideEffectFreeMethodsByType.keySet()) {
      sideEffectFreeMethods.addAll(sideEffectFreeMethodsByType.getValues(keyType));
    }

    RandoopListenerManager listenerMgr = new RandoopListenerManager();
    ForwardGenerator testGenerator =
        new ForwardGenerator(
            model,
            sideEffectFreeMethods,
            new GenInputsAbstract.Limits(),
            componentMgr,
            listenerMgr,
            operationModel.getClassTypes());
    GenTests genTests = new GenTests();

    TypedOperation objectConstructor;
    try {
      objectConstructor = TypedOperation.forConstructor(Object.class.getConstructor());
    } catch (NoSuchMethodException e) {
      fail("failed to get Object constructor: " + e);
      throw new Error("dead code");
    }

    Sequence newObj = new Sequence().extend(objectConstructor);
    Set<Sequence> excludeSet = new LinkedHashSet<>();
    excludeSet.add(newObj);

    Predicate<ExecutableSequence> isOutputTest =
        genTests.createTestOutputPredicate(
            excludeSet, operationModel.getCoveredClassesGoal(), require_classname_in_test);
    testGenerator.setTestPredicate(isOutputTest);

    ContractSet contracts = operationModel.getContracts();
    TestCheckGenerator checkGenerator =
        GenTests.createTestCheckGenerator(
            visibility,
            contracts,
            sideEffectFreeMethodsByType,
            operationModel.getOmitMethodsPredicate());
    testGenerator.setTestCheckGenerator(checkGenerator);
    testGenerator.setExecutionVisitor(
        new CoveredClassVisitor(operationModel.getCoveredClassesGoal()));

    TestUtils.setAllLogs(testGenerator);

    return testGenerator;
  }
}
