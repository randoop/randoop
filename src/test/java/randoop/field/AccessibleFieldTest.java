package randoop.field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import randoop.types.ClassOrInterfaceType;
import randoop.types.NonParameterizedType;

public class AccessibleFieldTest {

  @Test
  public void inheritedMethods() {
    Class<?> c = ClassWithFields.class;
    ClassOrInterfaceType declaringType = new NonParameterizedType(c);
    try {
      AccessibleField pf1 = new AccessibleField(c.getField("oneField"), declaringType);
      AccessibleField pf1_2 = new AccessibleField(c.getField("oneField"), declaringType);
      AccessibleField pf2 = new AccessibleField(c.getField("fourField"), declaringType);
      AccessibleField pf10 = new AccessibleField(c.getField("tenField"), declaringType);

      //identity
      assertEquals("Object built from same field should be equal", pf1, pf1_2);
      assertFalse("Objects of different fields should not be equal", pf1.equals(pf2));
      assertEquals(
          "Objects build from same field should have same hashcode",
          pf1.hashCode(),
          pf1_2.hashCode());

      /*
       * Note: this test shrank because type information lifted beyond operation level.
       */

      assertTrue("field is not static ", !pf1.isStatic());
      assertTrue("field is static ", pf2.isStatic());
      assertTrue("field is not static ", !pf10.isStatic());

      assertTrue("field is not final ", !pf1.isFinal());
      assertTrue("field is not final", !pf2.isFinal());
      assertTrue("field is final ", pf10.isFinal());

    } catch (NoSuchFieldException e) {
      fail("test failed because field in test class not found");
    } catch (SecurityException e) {
      fail("test failed because of unexpected security exception");
    }
  }
}
