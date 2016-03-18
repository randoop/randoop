package randoop.instrument;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import randoop.ComponentManager;
import randoop.RandoopListenerManager;
import randoop.SeedSequences;
import randoop.main.GenInputsAbstract;
import randoop.main.GenTests;
import randoop.operation.ConstructorCall;
import randoop.operation.Operation;
import randoop.reflection.DefaultReflectionPredicate;
import randoop.reflection.OperationExtractor;
import randoop.reflection.PublicVisibilityPredicate;
import randoop.reflection.ReflectionPredicate;
import randoop.reflection.VisibilityPredicate;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.ForwardGenerator;
import randoop.sequence.Sequence;
import randoop.test.TestCheckGenerator;
import randoop.types.TypeNames;
import randoop.util.ReflectionExecutor;
import randoop.util.predicate.Predicate;

/**
 * Test special cases of "covered" class filtering.
 * Want to ensure behaves well when given abstract class and interface.
 */
public class SpecialCoveredClassTest {

  @Test
  public void abstractClassTest() {
    GenInputsAbstract.silently_ignore_bad_class_names = false;
    GenInputsAbstract.classlist = new File("randoop/instrument/testcase/special-allclasses.txt");
    GenInputsAbstract.include_if_class_exercised =
        new File("randoop/instrument/testcase/special-coveredclasses.txt");
    ReflectionExecutor.usethreads = false;
    GenInputsAbstract.outputlimit = 5000;
    GenInputsAbstract.inputlimit = 10000;

    Set<Class<?>> coveredClasses = new LinkedHashSet<>();
    Set<Class<?>> classes = new LinkedHashSet<>();
    Set<String> omitfields = new HashSet<>();
    VisibilityPredicate visibility = new PublicVisibilityPredicate();
    GenTests.getClassesUnderTest(visibility, classes, coveredClasses);
    //
    assertTrue("should be one covered classes", coveredClasses.size() == 1);
    for (Class<?> c : coveredClasses) {
      assertEquals(
          "name should be AbstractTarget",
          "randoop.instrument.testcase.AbstractTarget",
          c.getName());
    }

    assertEquals("should have classes", 2, classes.size());
    for (Class<?> c : classes) {
      assertTrue("should not be interface: " + c.getName(), !c.isInterface());
    }
    //
    ReflectionPredicate predicate =
        new DefaultReflectionPredicate(GenInputsAbstract.omitmethods, omitfields, visibility);
    List<Operation> model = OperationExtractor.getOperations(classes, predicate);
    //
    assertEquals("model operations", 5, model.size());
    //
    Collection<Sequence> components = new LinkedHashSet<Sequence>();
    components.addAll(SeedSequences.objectsToSeeds(SeedSequences.primitiveSeeds));
    ComponentManager componentMgr = new ComponentManager(components);
    RandoopListenerManager listenerMgr = new RandoopListenerManager();
    ForwardGenerator testGenerator =
        new ForwardGenerator(
            model,
            GenInputsAbstract.timelimit * 1000,
            GenInputsAbstract.inputlimit,
            GenInputsAbstract.outputlimit,
            componentMgr,
            null,
            listenerMgr);
    GenTests genTests = new GenTests();
    ConstructorCall objectConstructor = null;
    try {
      objectConstructor = ConstructorCall.createConstructorCall(Object.class.getConstructor());
      if (!model.contains(objectConstructor)) model.add(objectConstructor);
    } catch (Exception e) {
      fail("couldn't get object constructor");
    }
    Predicate<ExecutableSequence> isOutputTest =
        genTests.createTestOutputPredicate(objectConstructor, coveredClasses, include_if_classname_appears);
    testGenerator.addTestPredicate(isOutputTest);
    TestCheckGenerator checkGenerator = genTests.createTestCheckGenerator(visibility, classes);
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

    Set<Operation> opSet = new LinkedHashSet<>();
    for (ExecutableSequence e : rTests) {
      assertTrue("should cover the class: " + at.getName(), e.coversClass(at));
      for (int i = 0; i < e.sequence.size(); i++) {
        Operation op = e.sequence.getStatement(i).getOperation();
        if (model.contains(op)) {
          opSet.add(op);
        }
      }
    }

    Class<?> it = null;
    try {
      it = TypeNames.getTypeForName("randoop.instrument.testcase.ImplementorOfTarget");
    } catch (ClassNotFoundException e) {
      fail("cannot find implementor class " + e);
    }
    for (Operation op : model) {
      assertTrue(
          "all model operations should be used or from wrong implementor",
          opSet.contains(op) || op.getDeclaringClass().equals(it));
    }
  }
}
