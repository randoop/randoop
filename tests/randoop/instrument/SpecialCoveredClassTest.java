package randoop.instrument;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Test;

import randoop.main.GenInputsAbstract;
import randoop.main.GenTests;
import randoop.reflection.PublicVisibilityPredicate;
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

    assertTrue("should be no covered classes", coveredClasses.isEmpty());

    for (Class<?> c : classes) {
      assertTrue("should not be abstract: " + c.getName(), ! Modifier.isAbstract(c.getModifiers()));
      assertTrue("should not be interface: " + c.getName(), ! c.isInterface());
    }
  }

}
