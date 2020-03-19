package randoop.instrument;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static randoop.reflection.VisibilityPredicate.IS_PUBLIC;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import org.checkerframework.checker.signature.qual.ClassGetName;
import org.junit.Test;
import randoop.generation.ComponentManager;
import randoop.generation.ForwardGenerator;
import randoop.generation.RandoopListenerManager;
import randoop.generation.SeedSequences;
import randoop.generation.TestUtils;
import randoop.main.ClassNameErrorHandler;
import randoop.main.GenInputsAbstract;
import randoop.main.GenTests;
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
import randoop.types.ClassOrInterfaceType;
import randoop.types.Type;
import randoop.util.MultiMap;
import randoop.util.ReflectionExecutor;

/**
 * Test special cases of covered class filtering. Want to ensure behaves well when given abstract
 * class and interface.
 */
public class SpecialCoveredClassTest {

  @Test
  public void abstractClassTest()
      throws ClassNotFoundException, NoSuchMethodException, SignatureParseException {
    GenInputsAbstract.silently_ignore_bad_class_names = false;
    GenInputsAbstract.classlist = Paths.get("instrument/testcase/special-allclasses.txt");
    GenInputsAbstract.require_covered_classes =
        Paths.get("instrument/testcase/special-coveredclasses.txt");
    ReflectionExecutor.usethreads = false;
    GenInputsAbstract.generated_limit = 10000;
    GenInputsAbstract.output_limit = 5000;
    randoop.util.Randomness.setSeed(0);

    VisibilityPredicate visibility = IS_PUBLIC;
    Set<@ClassGetName String> classnames = GenInputsAbstract.getClassnamesFromArgs(visibility);
    Set<@ClassGetName String> coveredClassnames =
        GenInputsAbstract.getClassNamesFromFile(GenInputsAbstract.require_covered_classes);
    Set<String> omitFields = new HashSet<>();
    ReflectionPredicate reflectionPredicate = new DefaultReflectionPredicate(omitFields);

    ClassNameErrorHandler classNameErrorHandler = new ThrowClassNameError();

    OperationModel operationModel =
        OperationModel.createModel(
            visibility,
            reflectionPredicate,
            GenInputsAbstract.omit_methods,
            classnames,
            coveredClassnames,
            classNameErrorHandler,
            GenInputsAbstract.literals_file);

    Set<Class<?>> coveredClassesGoal = operationModel.getCoveredClassesGoal();
    assertEquals(1, coveredClassesGoal.size());
    for (Class<?> c : coveredClassesGoal) {
      assertEquals("instrument.testcase.AbstractTarget", c.getName());
    }

    Set<ClassOrInterfaceType> classes = operationModel.getClassTypes();
    assertEquals(3, classes.size()); // 2 classes plus Object
    for (Type c : classes) {
      assertTrue("should not be interface: " + c.getBinaryName(), !c.isInterface());
    }

    List<TypedOperation> model = operationModel.getOperations();
    assertEquals(7, model.size());

    Set<Sequence> components = new LinkedHashSet<>();
    components.addAll(SeedSequences.defaultSeeds());
    components.addAll(operationModel.getAnnotatedTestValues());

    ComponentManager componentMgr = new ComponentManager(components);
    operationModel.addClassLiterals(
        componentMgr, GenInputsAbstract.literals_file, GenInputsAbstract.literals_level);

    RandoopListenerManager listenerMgr = new RandoopListenerManager();
    Set<TypedOperation> sideEffectFreeMethods = new LinkedHashSet<>();
    ForwardGenerator testGenerator =
        new ForwardGenerator(
            model,
            sideEffectFreeMethods,
            new GenInputsAbstract.Limits(),
            componentMgr,
            listenerMgr,
            operationModel.getClassTypes());
    GenTests genTests = new GenTests();

    TypedOperation objectConstructor = TypedOperation.forConstructor(Object.class.getConstructor());

    Set<Sequence> excludeSet = new LinkedHashSet<>();
    excludeSet.add(new Sequence().extend(objectConstructor));

    Predicate<ExecutableSequence> isOutputTest =
        genTests.createTestOutputPredicate(
            excludeSet,
            operationModel.getCoveredClassesGoal(),
            GenInputsAbstract.require_classname_in_test);
    testGenerator.setTestPredicate(isOutputTest);
    ContractSet contracts = operationModel.getContracts();
    TestCheckGenerator checkGenerator =
        GenTests.createTestCheckGenerator(
            visibility, contracts, new MultiMap<>(), operationModel.getOmitMethodsPredicate());
    testGenerator.setTestCheckGenerator(checkGenerator);
    testGenerator.setExecutionVisitor(new CoveredClassVisitor(coveredClassesGoal));
    TestUtils.setAllLogs(testGenerator);
    // for debugging:  operationModel.dumpModel();
    testGenerator.createAndClassifySequences();
    //    testGenerator.getOperationHistory().outputTable();

    List<ExecutableSequence> rTests = testGenerator.getRegressionSequences();
    System.out.println("number of regression tests: " + rTests.size());
    assertFalse(rTests.isEmpty());

    List<ExecutableSequence> eTests = testGenerator.getErrorTestSequences();
    CoveredClassTest.assertNoTests(eTests, "error");

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
      System.out.println("Unused operations: " + unused);
      System.out.println("Number of tests: " + rTests.size());
      for (ExecutableSequence rTest : rTests) {
        System.out.println("TEST:");
        System.out.println(rTest);
      }
      throw new Error("Unused operations: " + unused);
    } else {
      // TEMPORARY
      if (false) {
        System.out.println("Number of tests: " + rTests.size());
        for (ExecutableSequence rTest : rTests) {
          System.out.println("TEST:");
          System.out.println(rTest);
        }
      }
    }
  }
}
