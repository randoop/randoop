package randoop.instrument;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

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
import randoop.util.predicate.Predicate;

/**
 *
 */
public class CoveredClassTest {

  @Test
  public void testNoFilter() {
    System.out.println("no filter");
    GenInputsAbstract.outputlimit = 5000;
    GenInputsAbstract.inputlimit = 10000;
    GenInputsAbstract.silently_ignore_bad_class_names = false;
    GenInputsAbstract.classlist = new File("tests/randoop/instrument/testcase/allclasses.txt");
    GenInputsAbstract.include_if_classname_appears = null;
    GenInputsAbstract.include_if_class_exercised = null;
    // setup classes

    ForwardGenerator testGenerator = getGenerator();

    testGenerator.explore();
    List<ExecutableSequence> rTests = testGenerator.getRegressionSequences();
    List<ExecutableSequence> eTests = testGenerator.getErrorTestSequences();

    System.out.println("number of regression tests: " + rTests.size());
    assertTrue("should have some regression tests", rTests.size() > 0);
    assertFalse("don't expect error tests", eTests.size() > 0);

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
    GenInputsAbstract.outputlimit = 5000;
    GenInputsAbstract.inputlimit = 10000;
    GenInputsAbstract.silently_ignore_bad_class_names = false;
    GenInputsAbstract.classlist = new File("tests/randoop/instrument/testcase/allclasses.txt");
    GenInputsAbstract.include_if_classname_appears = Pattern.compile("randoop\\.instrument\\.testcase\\.A"); //null;
    GenInputsAbstract.include_if_class_exercised = null; //"tests/randoop/instrument/testcase/coveredclasses.txt";
    // setup classes

    ForwardGenerator testGenerator = getGenerator();

    testGenerator.explore();
    List<ExecutableSequence> rTests = testGenerator.getRegressionSequences();
    List<ExecutableSequence> eTests = testGenerator.getErrorTestSequences();

    System.out.println("number of regression tests: " + rTests.size());
    assertTrue("should be no regression tests", rTests.size() == 0);
    assertFalse("should be no error tests", eTests.size() > 0);

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
    GenInputsAbstract.outputlimit = 5000;
    GenInputsAbstract.inputlimit = 10000;
    GenInputsAbstract.silently_ignore_bad_class_names = false;
    GenInputsAbstract.classlist = new File("tests/randoop/instrument/testcase/allclasses.txt");
    GenInputsAbstract.include_if_classname_appears = null;
    GenInputsAbstract.include_if_class_exercised = new File("tests/randoop/instrument/testcase/coveredclasses.txt");
    // setup classes

    ForwardGenerator testGenerator = getGenerator();

    testGenerator.explore();
    List<ExecutableSequence> rTests = testGenerator.getRegressionSequences();
    List<ExecutableSequence> eTests = testGenerator.getErrorTestSequences();

    System.out.println("number of regression tests: " + rTests.size());
    assertTrue("should have some regression tests", rTests.size() > 0);
    assertFalse("don't expect error tests", eTests.size() > 0);

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
    Set<Class<?>> coveredClasses = new LinkedHashSet<>();
    Set<Class<?>> classes = new LinkedHashSet<>();
    Set<String> omitfields = new HashSet<>();
    VisibilityPredicate visibility = new PublicVisibilityPredicate();

    GenTests.getClassesUnderTest(visibility, classes, coveredClasses);
    ReflectionPredicate predicate = new DefaultReflectionPredicate(GenInputsAbstract.omitmethods, omitfields, visibility);
    List<Operation> model = OperationExtractor.getOperations(classes, predicate);
    Collection<Sequence> components = new LinkedHashSet<Sequence>();
    components.addAll(SeedSequences.objectsToSeeds(SeedSequences.primitiveSeeds));
    ComponentManager componentMgr = new ComponentManager(components );
    RandoopListenerManager listenerMgr = new RandoopListenerManager();
    ForwardGenerator testGenerator = new ForwardGenerator(
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
      if (!model.contains(objectConstructor))
        model.add(objectConstructor);
    } catch (Exception e) {
      fail("couldn't get object constructor");
    }
    Predicate<ExecutableSequence> isOutputTest = genTests.createTestOutputPredicate(objectConstructor, coveredClasses);
    testGenerator.addTestPredicate(isOutputTest);
    TestCheckGenerator checkGenerator = genTests.createTestCheckGenerator(visibility, classes);
    testGenerator.addTestCheckGenerator(checkGenerator);
    testGenerator.addExecutionVisitor(new ExercisedClassVisitor(coveredClasses));
    return testGenerator;
  }

}
