package randoop.instrument;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import randoop.main.GenInputsAbstract;
import randoop.main.GenTests;
import randoop.operation.Operation;
import randoop.reflection.DefaultReflectionPredicate;
import randoop.reflection.OperationExtractor;
import randoop.reflection.PublicVisibilityPredicate;
import randoop.reflection.ReflectionPredicate;
import randoop.reflection.VisibilityPredicate;

/**
 * Test special cases of "covered" class filtering.
 * Want to ensure behaves well when given abstract class and interface.
 */
public class SpecialCoveredClassTest {

  @Test
  public void abstractClassTest() {
    GenInputsAbstract.silently_ignore_bad_class_names = false;
    GenInputsAbstract.classlist = new File("tests/randoop/instrument/testcase/special-allclasses.txt");
    GenInputsAbstract.include_if_class_exercised = new File("tests/randoop/instrument/testcase/special-coveredclasses.txt");

    Set<Class<?>> coveredClasses = new LinkedHashSet<>();
    Set<Class<?>> classes = new LinkedHashSet<>();
    Set<String> omitfields = new HashSet<>();
    VisibilityPredicate visibility = new PublicVisibilityPredicate();

    GenTests.getClassesUnderTest(visibility, classes, coveredClasses);

    assertTrue("should be one covered classes", coveredClasses.size() == 1);
    for (Class<?> c : coveredClasses) {
      assertEquals("name should be AbstractTarget", "randoop.instrument.testcase.AbstractTarget", c.getName());
    }

    assertTrue("should be three classes", classes.size() == 3);
    for (Class<?> c : classes) {
      assertTrue("should not be interface: " + c.getName(), ! c.isInterface());
    }

    ReflectionPredicate predicate = new DefaultReflectionPredicate(GenInputsAbstract.omitmethods, omitfields, visibility);
    List<Operation> model = OperationExtractor.getOperations(classes, predicate);

    assertEquals("model should have six operations", 5, model.size());


  }

}
