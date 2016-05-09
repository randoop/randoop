package randoop.instrument;

import org.junit.Test;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import randoop.contract.ObjectContract;
import randoop.generation.ComponentManager;
import randoop.generation.ForwardGenerator;
import randoop.generation.RandoopListenerManager;
import randoop.generation.SeedSequences;
import randoop.main.ClassNameErrorHandler;
import randoop.main.GenInputsAbstract;
import randoop.main.GenTests;
import randoop.main.ThrowClassNameError;
import randoop.operation.TypedClassOperation;
import randoop.operation.TypedOperation;
import randoop.reflection.DefaultReflectionPredicate;
import randoop.reflection.OperationModel;
import randoop.reflection.PublicVisibilityPredicate;
import randoop.reflection.ReflectionPredicate;
import randoop.reflection.VisibilityPredicate;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Sequence;
import randoop.test.TestCheckGenerator;
import randoop.types.ClassOrInterfaceType;
import randoop.types.GeneralType;
import randoop.types.RandoopTypeException;
import randoop.types.TypeNames;
import randoop.util.MultiMap;
import randoop.util.ReflectionExecutor;
import randoop.util.predicate.Predicate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static randoop.main.GenInputsAbstract.include_if_class_exercised;
import static randoop.main.GenInputsAbstract.include_if_classname_appears;
import static randoop.main.GenInputsAbstract.methodlist;
import static randoop.main.GenInputsAbstract.omitmethods;

/**
 * Test special cases of "covered" (or exercised) class filtering.
 * Want to ensure behaves well when given abstract class and interface.
 */
public class SpecialCoveredClassTest {

  @Test
  public void abstractClassTest() {
    GenInputsAbstract.silently_ignore_bad_class_names = false;
    GenInputsAbstract.classlist = new File("randoop/instrument/testcase/special-allclasses.txt");
    include_if_class_exercised =
        new File("randoop/instrument/testcase/special-coveredclasses.txt");
    ReflectionExecutor.usethreads = false;
    GenInputsAbstract.outputlimit = 5000;
    GenInputsAbstract.inputlimit = 10000;

    Set<String> classnames = GenInputsAbstract.getClassnamesFromArgs();
    Set<String> coveredClassnames =
            GenInputsAbstract.getStringSetFromFile(
                    include_if_class_exercised, "Unable to read coverage class names");
    Set<String> omitFields = new HashSet<>();
    VisibilityPredicate visibility = new PublicVisibilityPredicate();
    ReflectionPredicate reflectionPredicate =
            new DefaultReflectionPredicate(omitmethods, omitFields);
    Set<String> methodSignatures =
            GenInputsAbstract.getStringSetFromFile(methodlist, "Error while reading method list file");
    ClassNameErrorHandler classNameErrorHandler = new ThrowClassNameError();
    OperationModel operationModel = null;
    try {
      operationModel=
              OperationModel.createModel(
                      visibility,
                      reflectionPredicate,
                      classnames,
                      coveredClassnames,
                      methodSignatures,
                      classNameErrorHandler,
                      GenInputsAbstract.literals_file);
    } catch (Throwable e) {
     fail("Error: " + e);
    }
    assert operationModel != null;

    Set<Class<?>> coveredClasses = operationModel.getExercisedClasses();
    Set<ClassOrInterfaceType> classes = operationModel.getClasses();
    //
    assertTrue("should be one covered classes", coveredClasses.size() == 1);
    for (Class<?> c : coveredClasses) {
      assertEquals(
          "name should be AbstractTarget",
          "randoop.instrument.testcase.AbstractTarget",
          c.getName());
    }

    // 2 classes plus Object
    assertEquals("should have classes", 3, classes.size());
    for (GeneralType c : classes) {
      assertTrue("should not be interface: " + c.getName(), !c.isInterface());
    }
    //
    List<TypedOperation> model = operationModel.getConcreteOperations();
    //
    assertEquals("model operations", 6, model.size());
    //
    Set<Sequence> components = new LinkedHashSet<>();
    components.addAll(SeedSequences.defaultSeeds());
    components.addAll(operationModel.getAnnotatedTestValues());

    ComponentManager componentMgr = new ComponentManager(components);
    operationModel.addClassLiterals(
            componentMgr, GenInputsAbstract.literals_file, GenInputsAbstract.literals_level);

    RandoopListenerManager listenerMgr = new RandoopListenerManager();
    Set<TypedOperation> observers = new LinkedHashSet<>();
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
      assert false : "failed to get Object constructor: " + e;
    }
    assert objectConstructor != null;

    Sequence newObj = new Sequence().extend(objectConstructor);
    Set<Sequence> excludeSet = new LinkedHashSet<>();
    excludeSet.add(newObj);

    Predicate<ExecutableSequence> isOutputTest =
            genTests.createTestOutputPredicate(
                    excludeSet, operationModel.getExercisedClasses(), include_if_classname_appears);
    testGenerator.addTestPredicate(isOutputTest);
    Set<ObjectContract> contracts = operationModel.getContracts();
    Set<TypedOperation> excludeAsObservers = new LinkedHashSet<>();
    MultiMap<GeneralType,TypedOperation> observerMap = new MultiMap<>();
    TestCheckGenerator checkGenerator = genTests.createTestCheckGenerator(visibility, contracts, observerMap, excludeAsObservers);
    testGenerator.addTestCheckGenerator(checkGenerator);
    testGenerator.addExecutionVisitor(new ExercisedClassVisitor(coveredClasses));
    testGenerator.explore();
    List<ExecutableSequence> rTests = testGenerator.getRegressionSequences();
    List<ExecutableSequence> eTests = testGenerator.getErrorTestSequences();
    //
    System.out.println("number of regression tests: " + rTests.size());
    assertTrue("should have some regression tests", rTests.size() > 0);
    assertFalse("don't expect error tests", eTests.size() > 0);
    //
    Class<?> at = null;
    try {
      at = TypeNames.getTypeForName("randoop.instrument.testcase.AbstractTarget");
    } catch (ClassNotFoundException e) {
      fail("cannot find class: " + e);
    }

    Set<TypedOperation> opSet = new LinkedHashSet<>();
    for (ExecutableSequence e : rTests) {
      assertTrue("should cover the class: " + at.getName(), e.coversClass(at));
      for (int i = 0; i < e.sequence.size(); i++) {
        TypedOperation op = e.sequence.getStatement(i).getOperation();
        if (model.contains(op)) {
          opSet.add(op);
        }
      }
    }

    GeneralType it = null;
    try {
      it = GeneralType.forName("randoop.instrument.testcase.ImplementorOfTarget");
    } catch (ClassNotFoundException e) {
      fail("cannot find implementor class " + e);
    }
    for (TypedOperation op : model) {
      if (op instanceof TypedClassOperation) {
        assertTrue(
                "all model operations should be used or from wrong implementor",
                opSet.contains(op) || ((TypedClassOperation)op).getDeclaringType().equals(it));
      } else {
        assertTrue("all model operations should be used", opSet.contains(op));
      }
    }
  }
}
