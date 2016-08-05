package randoop.instrument;

import org.junit.Test;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import randoop.CheckRep;
import randoop.main.GenInputsAbstract;
import randoop.reflection.TypeNames;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * This test was originally written when the exercised-class instrumentation
 * was being handled by a classloader, and the CheckRep annotation was lost.
 * This is here mainly to make sure that annotations are still arriving when
 * the transforming java agent is used.
 */
public class LoadingWithAnnotationTest {

  @Test
  public void test() {
    GenInputsAbstract.silently_ignore_bad_class_names = false;
    GenInputsAbstract.classlist = new File("randoop/instrument/testcase/allclasses.txt");
    GenInputsAbstract.include_if_classname_appears = null;
    GenInputsAbstract.include_if_class_exercised =
        new File("randoop/instrument/testcase/annotatedclasses.txt");

    Class<?> cc = null;
    try {
      cc = TypeNames.getTypeForName("randoop.instrument.testcase.D");
    } catch (ClassNotFoundException e) {
      fail("cannot find class: " + e);
    }

    Method m = null;
    try {
      m = cc.getDeclaredMethod("isZero");
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
    Annotation[] annotations = m.getAnnotations();
    assertTrue("should be one annotation", annotations.length == 1);

    for (Annotation a : annotations) {
      Class<?> annot_c = a.annotationType();
      assertEquals("name matches", "randoop.CheckRep", annot_c.getName());

      assertEquals("class should match once loaded", crc, annot_c);
      assertEquals("class should match", c, annot_c);
    }
  }
}
