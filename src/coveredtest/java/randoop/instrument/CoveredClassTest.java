package randoop.instrument;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static randoop.main.GenInputsAbstract.methodlist;
import static randoop.main.GenInputsAbstract.require_classname_in_test;
import static randoop.reflection.VisibilityPredicate.IS_PUBLIC;

import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
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
import randoop.operation.OperationParseException;
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

  @Test
  public void testNoFilter() {
    System.out.println("no filter");

    GenInputsAbstract.classlist = Paths.get("instrument/testcase/allclasses.txt");
    require_classname_in_test = null;
    GenInputsAbstract.require_covered_classes = null;
    // setup classes

    ForwardGenerator testGenerator = getGeneratorForTest();

    testGenerator.createAndClassifySequences();
    List<ExecutableSequence> rTests = testGenerator.getRegressionSequences();
    List<ExecutableSequence> eTests = testGenerator.getErrorTestSequences();

    System.out.println("number of regression tests: " + rTests.size());
    assertTrue("should have some regression tests", !rTests.isEmpty());
    assertFalse("don't expect error tests", !eTests.isEmpty());

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
    System.out.println("name filter");
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
    assertTrue("should be no regression tests", rTests.isEmpty());
    assertFalse("should be no error tests", !eTests.isEmpty());

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
    System.out.println("coverage filter");
    GenInputsAbstract.classlist = Paths.get("instrument/testcase/allclasses.txt");
    require_classname_in_test = null;
    GenInputsAbstract.require_covered_classes = Paths.get("instrument/testcase/coveredclasses.txt");
    // setup classes

    ForwardGenerator testGenerator = getGeneratorForTest();

    testGenerator.createAndClassifySequences();
    List<ExecutableSequence> rTests = testGenerator.getRegressionSequences();
    List<ExecutableSequence> eTests = testGenerator.getErrorTestSequences();

    System.out.println("number of regression tests: " + rTests.size());
    assertTrue("should have some regression tests", !rTests.isEmpty());
    assertFalse("don't expect error tests", !eTests.isEmpty());

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
    Set<String> classnames = GenInputsAbstract.getClassnamesFromArgs();
    Set<String> coveredClassnames =
        GenInputsAbstract.getStringSetFromFile(
            GenInputsAbstract.require_covered_classes, "coverage class names");
    Set<String> omitFields =
        GenInputsAbstract.getStringSetFromFile(GenInputsAbstract.omit_field_list, "field list");
    VisibilityPredicate visibility = IS_PUBLIC;
    ReflectionPredicate reflectionPredicate = new DefaultReflectionPredicate(omitFields);
    ClassNameErrorHandler classNameErrorHandler = new ThrowClassNameError();
    Set<String> methodSignatures =
        GenInputsAbstract.getStringSetFromFile(methodlist, "method list");

    OperationModel operationModel;
    try {
      operationModel =
          OperationModel.createModel(
              visibility,
              reflectionPredicate,
              GenInputsAbstract.omitmethods,
              classnames,
              coveredClassnames,
              methodSignatures,
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
    assert operationModel != null;

    List<TypedOperation> model = operationModel.getOperations();
    Set<Sequence> components = new LinkedHashSet<>();
    components.addAll(SeedSequences.defaultSeeds());
    components.addAll(operationModel.getAnnotatedTestValues());

    ComponentManager componentMgr = new ComponentManager(components);
    operationModel.addClassLiterals(
        componentMgr, GenInputsAbstract.literals_file, GenInputsAbstract.literals_level);

    Set<String> observerSignatures =
        GenInputsAbstract.getStringSetFromFile(
            GenInputsAbstract.observers, "observer", "//.*", null);

    // Maps each class type to the observer methods in it.
    MultiMap<Type, TypedOperation> observerMap;
    try {
      observerMap = operationModel.getObservers(observerSignatures);
    } catch (OperationParseException e) {
      System.out.printf("Parse error while reading observers: %s%n", e);
      System.exit(1);
      throw new Error("dead code");
    }
    assert observerMap != null;
    Set<TypedOperation> observers = new LinkedHashSet<>();
    for (Type keyType : observerMap.keySet()) {
      observers.addAll(observerMap.getValues(keyType));
    }

    RandoopListenerManager listenerMgr = new RandoopListenerManager();
    ForwardGenerator testGenerator =
        new ForwardGenerator(
            model,
            observers,
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
        GenTests.createTestCheckGenerator(visibility, contracts, observerMap);
    testGenerator.setTestCheckGenerator(checkGenerator);
    testGenerator.setExecutionVisitor(
        new CoveredClassVisitor(operationModel.getCoveredClassesGoal()));

    TestUtils.setAllLogs(testGenerator);

    return testGenerator;
  }
}
