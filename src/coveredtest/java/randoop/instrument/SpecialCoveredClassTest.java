package randoop.instrument;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;
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
  public void abstractClassTest() {
    GenInputsAbstract.silently_ignore_bad_class_names = false;
    GenInputsAbstract.classlist = new File("instrument/testcase/special-allclasses.txt");
    GenInputsAbstract.require_covered_classes =
        new File("instrument/testcase/special-coveredclasses.txt");
    ReflectionExecutor.usethreads = false;
    GenInputsAbstract.generatedLimit = 10000;
    GenInputsAbstract.outputLimit = 5000;

    Set<String> classnames = GenInputsAbstract.getClassnamesFromArgs();
    Set<String> coveredClassnames =
        GenInputsAbstract.getStringSetFromFile(
            GenInputsAbstract.require_covered_classes, "Unable to read coverage class names");
    Set<String> omitFields = new HashSet<>();
    VisibilityPredicate visibility = new PublicVisibilityPredicate();
    ReflectionPredicate reflectionPredicate =
        new DefaultReflectionPredicate(GenInputsAbstract.omitmethods, omitFields);
    Set<String> methodSignatures =
        GenInputsAbstract.getStringSetFromFile(
            GenInputsAbstract.methodlist, "Error while reading method list file");
    ClassNameErrorHandler classNameErrorHandler = new ThrowClassNameError();
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
    } catch (Throwable e) {
      fail("Error: " + e);
    }
    assert operationModel != null;

    Set<Class<?>> coveredClasses = operationModel.getCoveredClasses();
    Set<ClassOrInterfaceType> classes = operationModel.getClassTypes();
    //
    assertThat("should be one covered classes", coveredClasses.size(), is(equalTo(1)));
    for (Class<?> c : coveredClasses) {
      assertEquals(
          "name should be AbstractTarget", "instrument.testcase.AbstractTarget", c.getName());
    }

    // 2 classes plus Object
    assertEquals("should have classes", 3, classes.size());
    for (Type c : classes) {
      assertTrue("should not be interface: " + c.getName(), !c.isInterface());
    }
    //
    List<TypedOperation> model = operationModel.getOperations();
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
            model, observers, new GenInputsAbstract.Limits(), componentMgr, listenerMgr);
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
            excludeSet,
            operationModel.getCoveredClasses(),
            GenInputsAbstract.require_classname_in_test);
    testGenerator.addTestPredicate(isOutputTest);
    ContractSet contracts = operationModel.getContracts();
    Set<TypedOperation> excludeAsObservers = new LinkedHashSet<>();
    MultiMap<Type, TypedOperation> observerMap = new MultiMap<>();
    TestCheckGenerator checkGenerator =
        genTests.createTestCheckGenerator(visibility, contracts, observerMap, excludeAsObservers);
    testGenerator.addTestCheckGenerator(checkGenerator);
    testGenerator.addExecutionVisitor(new CoveredClassVisitor(coveredClasses));
    testGenerator.explore();
    List<ExecutableSequence> rTests = testGenerator.getRegressionSequences();
    List<ExecutableSequence> eTests = testGenerator.getErrorTestSequences();
    //
    System.out.println("number of regression tests: " + rTests.size());
    assertTrue("should have some regression tests", !rTests.isEmpty());
    assertFalse("don't expect error tests", !eTests.isEmpty());
    //
    Class<?> at = null;
    try {
      at = TypeNames.getTypeForName("instrument.testcase.AbstractTarget");
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

    Type it = null;
    try {
      it = Type.forName("instrument.testcase.ImplementorOfTarget");
    } catch (ClassNotFoundException e) {
      fail("cannot find implementor class " + e);
    }
    for (TypedOperation op : model) {
      if (op instanceof TypedClassOperation) {
        assertTrue(
            "all model operations should be used or from wrong implementor",
            opSet.contains(op) || ((TypedClassOperation) op).getDeclaringType().equals(it));
      } else {
        assertTrue("all model operations should be used", opSet.contains(op));
      }
    }
  }
}
