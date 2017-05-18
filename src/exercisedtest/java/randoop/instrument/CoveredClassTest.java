package randoop.instrument;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static randoop.main.GenInputsAbstract.include_if_classname_appears;
import static randoop.main.GenInputsAbstract.methodlist;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import randoop.generation.ComponentManager;
import randoop.generation.ForwardGenerator;
import randoop.generation.RandoopListenerManager;
import randoop.generation.SeedSequences;
import randoop.main.ClassNameErrorHandler;
import randoop.main.GenInputsAbstract;
import randoop.main.GenTests;
import randoop.main.OptionsCache;
import randoop.main.ThrowClassNameError;
import randoop.operation.OperationParseException;
import randoop.operation.TypedOperation;
import randoop.reflection.DefaultReflectionPredicate;
import randoop.reflection.OperationModel;
import randoop.reflection.PublicVisibilityPredicate;
import randoop.reflection.ReflectionPredicate;
import randoop.reflection.TypeNames;
import randoop.reflection.VisibilityPredicate;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Sequence;
import randoop.test.ContractSet;
import randoop.test.TestCheckGenerator;
import randoop.types.Type;
import randoop.util.MultiMap;
import randoop.util.predicate.Predicate;

public class CoveredClassTest {

  private static OptionsCache optionsCache;

  @BeforeClass
  public static void setup() {
    optionsCache = new OptionsCache();
    optionsCache.saveState();
    GenInputsAbstract.outputlimit = 5000;
    GenInputsAbstract.inputlimit = 10000;
    GenInputsAbstract.silently_ignore_bad_class_names = false;
  }

  @AfterClass
  public static void restore() {
    optionsCache.restoreState();
  }

  @Test
  public void testNoFilter() {
    System.out.println("no filter");

    GenInputsAbstract.classlist = new File("randoop/instrument/testcase/allclasses.txt");
    include_if_classname_appears = null;
    GenInputsAbstract.include_if_class_exercised = null;
    // setup classes

    ForwardGenerator testGenerator = getGenerator();

    testGenerator.explore();
    List<ExecutableSequence> rTests = testGenerator.getRegressionSequences();
    List<ExecutableSequence> eTests = testGenerator.getErrorTestSequences();

    System.out.println("number of regression tests: " + rTests.size());
    assertTrue("should have some regression tests", !rTests.isEmpty());
    assertFalse("don't expect error tests", !eTests.isEmpty());

    Class<?> ac = null;
    try {
      ac = TypeNames.getTypeForName("randoop.instrument.testcase.A");
    } catch (ClassNotFoundException e) {
      fail("cannot find class: " + e);
    }

    Class<?> cc = null;
    try {
      cc = TypeNames.getTypeForName("randoop.instrument.testcase.C");
    } catch (ClassNotFoundException e) {
      fail("cannot find class: " + e);
    }

    for (ExecutableSequence e : rTests) {
      assertFalse("should not cover the class: " + ac.getName(), e.coversClass(ac));
      assertFalse("should not cover the class: " + cc.getName(), e.coversClass(cc));
    }
  }

  @Test
  public void testNameFilter() {
    System.out.println("name filter");
    GenInputsAbstract.classlist = new File("randoop/instrument/testcase/allclasses.txt");
    include_if_classname_appears = Pattern.compile("randoop\\.instrument\\.testcase\\.A"); //null;
    GenInputsAbstract.include_if_class_exercised =
        null; //"tests/randoop/instrument/testcase/coveredclasses.txt";
    // setup classes

    ForwardGenerator testGenerator = getGenerator();

    testGenerator.explore();
    List<ExecutableSequence> rTests = testGenerator.getRegressionSequences();
    List<ExecutableSequence> eTests = testGenerator.getErrorTestSequences();

    System.out.println("number of regression tests: " + rTests.size());
    assertTrue("should be no regression tests", rTests.isEmpty());
    assertFalse("should be no error tests", !eTests.isEmpty());

    Class<?> ac = null;
    try {
      ac = TypeNames.getTypeForName("randoop.instrument.testcase.A");
    } catch (ClassNotFoundException e) {
      fail("cannot find class: " + e);
    }

    Class<?> cc = null;
    try {
      cc = TypeNames.getTypeForName("randoop.instrument.testcase.C");
    } catch (ClassNotFoundException e) {
      fail("cannot find class: " + e);
    }

    for (ExecutableSequence e : rTests) {
      assertFalse("should not cover the class: " + ac.getName(), e.coversClass(ac));
      assertFalse("should not cover the class: " + cc.getName(), e.coversClass(cc));
    }
  }

  @Test
  public void testCoverageFilter() {
    System.out.println("coverage filter");
    GenInputsAbstract.classlist = new File("randoop/instrument/testcase/allclasses.txt");
    include_if_classname_appears = null;
    GenInputsAbstract.include_if_class_exercised =
        new File("randoop/instrument/testcase/coveredclasses.txt");
    // setup classes

    ForwardGenerator testGenerator = getGenerator();

    testGenerator.explore();
    List<ExecutableSequence> rTests = testGenerator.getRegressionSequences();
    List<ExecutableSequence> eTests = testGenerator.getErrorTestSequences();

    System.out.println("number of regression tests: " + rTests.size());
    assertTrue("should have some regression tests", !rTests.isEmpty());
    assertFalse("don't expect error tests", !eTests.isEmpty());

    Class<?> ac = null;
    try {
      ac = TypeNames.getTypeForName("randoop.instrument.testcase.A");
    } catch (ClassNotFoundException e) {
      fail("cannot find class: " + e);
    }

    Class<?> cc = null;
    try {
      cc = TypeNames.getTypeForName("randoop.instrument.testcase.C");
    } catch (ClassNotFoundException e) {
      fail("cannot find class: " + e);
    }

    for (ExecutableSequence e : rTests) {
      assertTrue("should cover the class: " + ac.getName(), e.coversClass(ac));
      assertFalse("should not cover the class: " + cc.getName(), e.coversClass(cc));
    }
  }

  private ForwardGenerator getGenerator() {
    Set<String> classnames = GenInputsAbstract.getClassnamesFromArgs();
    Set<String> coveredClassnames =
        GenInputsAbstract.getStringSetFromFile(
            GenInputsAbstract.include_if_class_exercised, "unable to read coverage class names");
    Set<String> omitFields =
        GenInputsAbstract.getStringSetFromFile(
            GenInputsAbstract.omit_field_list, "Error reading field file");
    VisibilityPredicate visibility = new PublicVisibilityPredicate();
    ReflectionPredicate reflectionPredicate =
        new DefaultReflectionPredicate(GenInputsAbstract.omitmethods, omitFields);
    ClassNameErrorHandler classNameErrorHandler = new ThrowClassNameError();
    Set<String> methodSignatures =
        GenInputsAbstract.getStringSetFromFile(methodlist, "Error while reading method list file");

    OperationModel operationModel = null;
    try {
      operationModel =
          OperationModel.createModel(
              visibility,
              reflectionPredicate,
              classnames,
              coveredClassnames,
              methodSignatures,
              classNameErrorHandler,
              GenInputsAbstract.literals_file);
    } catch (OperationParseException e) {
      fail("operation parse exception thrown: " + e);
    } catch (NoSuchMethodException e) {
      fail("Method not found: " + e);
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
            GenInputsAbstract.observers, "Unable to read observer file", "//.*", null);

    MultiMap<Type, TypedOperation> observerMap = null;
    try {
      observerMap = operationModel.getObservers(observerSignatures);
    } catch (OperationParseException e) {
      System.out.printf("Error: parse exception thrown while reading observers: %s%n", e);
      System.exit(1);
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
            GenInputsAbstract.timelimit * 1000,
            GenInputsAbstract.inputlimit,
            GenInputsAbstract.outputlimit,
            componentMgr,
            listenerMgr);
    GenTests genTests = new GenTests();

    TypedOperation objectConstructor = null;
    try {
      objectConstructor = TypedOperation.forConstructor(Object.class.getConstructor());
    } catch (NoSuchMethodException e) {
      fail("failed to get Object constructor: " + e);
    }
    assert objectConstructor != null : "object constructor is null";

    Sequence newObj = new Sequence().extend(objectConstructor);
    Set<Sequence> excludeSet = new LinkedHashSet<>();
    excludeSet.add(newObj);

    Predicate<ExecutableSequence> isOutputTest =
        genTests.createTestOutputPredicate(
            excludeSet, operationModel.getExercisedClasses(), include_if_classname_appears);
    testGenerator.addTestPredicate(isOutputTest);

    ContractSet contracts = operationModel.getContracts();
    Set<TypedOperation> excludeAsObservers = new LinkedHashSet<>();
    TestCheckGenerator checkGenerator =
        genTests.createTestCheckGenerator(visibility, contracts, observerMap, excludeAsObservers);
    testGenerator.addTestCheckGenerator(checkGenerator);
    testGenerator.addExecutionVisitor(
        new ExercisedClassVisitor(operationModel.getExercisedClasses()));
    return testGenerator;
  }
}
