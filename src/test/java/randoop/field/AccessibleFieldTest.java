package randoop.field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import randoop.types.ConcreteType;
import randoop.types.GeneralType;

public class AccessibleFieldTest {

  @Test
  public void inheritedMethods() {
    Class<?> c = ClassWithFields.class;
    GeneralType declaringType = ConcreteType.forClass(c);
    try {
      AccessibleField pf1 = new AccessibleField(c.getField("oneField"), declaringType);
      AccessibleField pf1_2 = new AccessibleField(c.getField("oneField"), declaringType);
      AccessibleField pf2 = new AccessibleField(c.getField("threeField"), declaringType);

      //identity
      assertEquals("Object built from same field should be equal", pf1, pf1_2);
      assertFalse("Objects of different fields should not be equal", pf1.equals(pf2));
      assertEquals(
          "Objects build from same field should have same hashcode",
          pf1.hashCode(),
          pf1_2.hashCode());

      /*
       * Note: this test shrank because
       */

    } catch (NoSuchFieldException e) {
      fail("test failed because field in test class not found");
    } catch (SecurityException e) {
      fail("test failed because of unexpected security exception");
    }
  }
}
