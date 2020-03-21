package randoop.field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import randoop.types.ClassOrInterfaceType;
import randoop.types.NonParameterizedType;

public class AccessibleFieldTest {

  @Test
  public void inheritedMethods() throws NoSuchFieldException, SecurityException {
    Class<?> c = ClassWithFields.class;
    ClassOrInterfaceType declaringType = new NonParameterizedType(c);

    AccessibleField pf1 = new AccessibleField(c.getField("oneField"), declaringType);
    AccessibleField pf1_2 = new AccessibleField(c.getField("oneField"), declaringType);
    AccessibleField pf2 = new AccessibleField(c.getField("fourField"), declaringType);
    AccessibleField pf10 = new AccessibleField(c.getField("tenField"), declaringType);

    // identity
    assertEquals(pf1, pf1_2);
    assertEquals(pf1.hashCode(), pf1_2.hashCode());

    assertNotEquals(pf1, pf2);

    /*
     * Note: this test shrank because type information lifted beyond operation level.
     */

    assertFalse(pf1.isStatic());
    assertTrue(pf2.isStatic());
    assertFalse(pf10.isStatic());

    assertFalse(pf1.isFinal());
    assertFalse(pf2.isFinal());
    assertTrue(pf10.isFinal());
  }
}
