package randoop.instrument;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Test;

import randoop.CheckRep;
import randoop.main.GenInputsAbstract;
import randoop.main.GenTests;
import randoop.reflection.PublicVisibilityPredicate;
import randoop.reflection.VisibilityPredicate;
import randoop.types.TypeNames;

public class LoadingWithAnnotationTest {

  @Test
  public void test() {
    GenInputsAbstract.silently_ignore_bad_class_names = false;
    GenInputsAbstract.classlist = new File("tests/randoop/instrument/testcase/allclasses.txt");
    GenInputsAbstract.include_if_classname_appears = null;
    GenInputsAbstract.include_if_class_exercised = new File("tests/randoop/instrument/testcase/annotatedclasses.txt");
    Set<Class<?>> coveredClasses = new LinkedHashSet<>();
    Set<Class<?>> classes = new LinkedHashSet<>();
    VisibilityPredicate visibility = new PublicVisibilityPredicate();
    GenTests.getClassesUnderTest(visibility, classes, coveredClasses);

    Class<?> cc = null;
    try {
      cc = TypeNames.getTypeForName("randoop.instrument.testcase.C");
    } catch (ClassNotFoundException e) {
      fail("cannot find class: " + e);
    }

    Method m = null;
    try {
      m = cc.getDeclaredMethod("isZero", new Class<?>[0]);
    } catch (NoSuchMethodException e) {
      fail("annotated method not found: " + e);
    } catch (SecurityException e) {
      fail("method not accessible: " + e);
    }

    Class<?> crc = null;
    try {
      crc = TypeNames.getTypeForName("randoop.CheckRep");
    } catch (ClassNotFoundException e) {
      fail("couldn't find checkrep: " + e);
    }

    Class<?> c = CheckRep.class;
    for (Annotation a : m.getAnnotations()) {
      Class<?> annot_c = a.annotationType();
      assertEquals("name matches", "randoop.CheckRep", annot_c.getName() );

      assertEquals("class should match once loaded", crc, annot_c);
      assertFalse("should not match loaded from default loader", c.equals(annot_c));
    }

  }

}
