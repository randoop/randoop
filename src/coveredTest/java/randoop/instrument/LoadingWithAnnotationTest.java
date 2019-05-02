package randoop.instrument;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.nio.file.Paths;
import org.junit.Test;
import randoop.CheckRep;
import randoop.main.GenInputsAbstract;
import randoop.reflection.TypeNames;

/**
 * This test was originally written when the covered-class instrumentation was being handled by a
 * classloader, and the CheckRep annotation was lost. This is here mainly to make sure that
 * annotations are still arriving when the transforming java agent is used.
 */
public class LoadingWithAnnotationTest {

  @Test
  public void test() {
    GenInputsAbstract.silently_ignore_bad_class_names = false;
    GenInputsAbstract.classlist = Paths.get("randoop/instrument/testcase/allclasses.txt");
    GenInputsAbstract.require_classname_in_test = null;
    GenInputsAbstract.require_covered_classes =
        Paths.get("randoop/instrument/testcase/annotatedclasses.txt");

    Class<?> cc = null;
    try {
      cc = TypeNames.getTypeForName("instrument.testcase.D");
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
