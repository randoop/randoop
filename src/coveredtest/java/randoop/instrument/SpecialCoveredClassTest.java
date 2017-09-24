package randoop.instrument;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.lang.ClassNotFoundException;
import java.lang.NoSuchMethodException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import randoop.generation.ComponentManager;
import randoop.generation.ForwardGenerator;
import randoop.generation.RandoopListenerManager;
import randoop.generation.SeedSequences;
import randoop.generation.TestUtils;
import randoop.main.ClassNameErrorHandler;
import randoop.main.GenInputsAbstract;
import randoop.main.GenTests;
import randoop.main.RandoopInputException;
import randoop.main.ThrowClassNameError;
import randoop.operation.TypedClassOperation;
import randoop.operation.TypedOperation;
import randoop.reflection.DefaultReflectionPredicate;
import randoop.reflection.OperationModel;
import randoop.reflection.PublicVisibilityPredicate;
import randoop.reflection.ReflectionPredicate;
import randoop.reflection.SignatureParseException;
import randoop.reflection.TypeNames;
import randoop.reflection.VisibilityPredicate;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Sequence;
import randoop.test.ContractSet;
import randoop.test.TestCheckGenerator;
import randoop.types.ClassOrInterfaceType;
import randoop.types.Type;
import randoop.util.MultiMap;
import randoop.util.ReflectionExecutor;
import randoop.util.predicate.Predicate;

/**
 * Test special cases of covered class filtering. Want to ensure behaves well when given abstract
 * class and interface.
 */
public class SpecialCoveredClassTest {

  @Test
  public void abstractClassTest()
      throws ClassNotFoundException, NoSuchMethodException, RandoopInputException,
          SignatureParseException {
    TestUtils.setSelectionLog();
    GenInputsAbstract.silently_ignore_bad_class_names = false;
    GenInputsAbstract.classlist = new File("instrument/testcase/special-allclasses.txt");
    GenInputsAbstract.require_covered_classes =
        new File("instrument/testcase/special-coveredclasses.txt");
    ReflectionExecutor.usethreads = false;
    GenInputsAbstract.generatedLimit = 10000;
    GenInputsAbstract.outputLimit = 5000;
    randoop.util.Randomness.setSeed(0);

    Set<String> classnames = GenInputsAbstract.getClassnamesFromArgs();
    Set<String> coveredClassnames =
        GenInputsAbstract.getStringSetFromFile(
            GenInputsAbstract.require_covered_classes, "coverage class names");
    VisibilityPredicate visibility = new PublicVisibilityPredicate();
    Set<String> omitFields = new HashSet<>();
    ReflectionPredicate reflectionPredicate = new DefaultReflectionPredicate(omitFields);
    Set<String> methodSignatures =
        GenInputsAbstract.getStringSetFromFile(GenInputsAbstract.methodlist, "method list");

    ClassNameErrorHandler classNameErrorHandler = new ThrowClassNameError();

    OperationModel operationModel =
        OperationModel.createModel(
            visibility,
            reflectionPredicate,
            GenInputsAbstract.omitmethods,
            classnames,
            coveredClassnames,
            methodSignatures,
            classNameErrorHandler,
            GenInputsAbstract.literals_file);

    Set<Class<?>> coveredClassesGoal = operationModel.getCoveredClassesGoal();
    assertEquals("should be one covered classes goal", coveredClassesGoal.size(), 1);
    for (Class<?> c : coveredClassesGoal) {
      assertEquals(
          "name should be AbstractTarget", "instrument.testcase.AbstractTarget", c.getName());
    }

    Set<ClassOrInterfaceType> classes = operationModel.getClassTypes();
    assertEquals("should have classes", 3, classes.size()); // 2 classes plus Object
    for (Type c : classes) {
      assertTrue("should not be interface: " + c.getName(), !c.isInterface());
    }

    List<TypedOperation> model = operationModel.getOperations();
    assertEquals("model operations", 7, model.size());

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
            model, observers, new GenInputsAbstract.Limits(), componentMgr, listenerMgr);
    GenTests genTests = new GenTests();

    TypedOperation objectConstructor = TypedOperation.forConstructor(Object.class.getConstructor());

    Set<Sequence> excludeSet = new LinkedHashSet<>();
    excludeSet.add(new Sequence().extend(objectConstructor));

    Predicate<ExecutableSequence> isOutputTest =
        genTests.createTestOutputPredicate(
            excludeSet,
            operationModel.getCoveredClassesGoal(),
            GenInputsAbstract.require_classname_in_test);
    testGenerator.addTestPredicate(isOutputTest);
    ContractSet contracts = operationModel.getContracts();
    MultiMap<Type, TypedOperation> observerMap = new MultiMap<>();
    TestCheckGenerator checkGenerator =
        genTests.createTestCheckGenerator(visibility, contracts, observerMap);
    testGenerator.addTestCheckGenerator(checkGenerator);
    testGenerator.addExecutionVisitor(new CoveredClassVisitor(coveredClassesGoal));
    TestUtils.setOperationLog(testGenerator);
    TestUtils.setSelectionLog();
    testGenerator.explore();
    //    testGenerator.getOperationHistory().outputTable();

    List<ExecutableSequence> rTests = testGenerator.getRegressionSequences();
    System.out.println("number of regression tests: " + rTests.size());
    assertTrue("should have some regression tests", !rTests.isEmpty());

    List<ExecutableSequence> eTests = testGenerator.getErrorTestSequences();
    assertFalse("don't expect error tests", !eTests.isEmpty());

    Class<?> atClass = TypeNames.getTypeForName("instrument.testcase.AbstractTarget");

    Set<TypedOperation> opSet = new LinkedHashSet<>();
    for (ExecutableSequence e : rTests) {
      assertTrue(e.coversClass(atClass));
      for (int i = 0; i < e.sequence.size(); i++) {
        TypedOperation op = e.sequence.getStatement(i).getOperation();
        if (model.contains(op)) {
          opSet.add(op);
        }
      }
    }

    List<TypedOperation> unused = new ArrayList<>();
    Type iotType = Type.forName("instrument.testcase.ImplementorOfTarget");
    for (TypedOperation op : model) {
      if (op instanceof TypedClassOperation) {
        if (!(opSet.contains(op)
            || ((TypedClassOperation) op).getDeclaringType().equals(iotType))) {
          unused.add(op);
        }
      } else {
        if (!opSet.contains(op)) {
          unused.add(op);
        }
      }
    }
    if (!unused.isEmpty()) {
      // TODO: could output the generated tests too.
      throw new Error("Unused operations: " + unused);
    }
  }
}
