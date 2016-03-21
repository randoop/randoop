package randoop.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;

import org.junit.Test;

public class ArrayTypeTest {

  @Test
  public void testAssignability() {
    ConcreteType intArrType = new ConcreteArrayType(new ConcreteSimpleType(int.class));
    ConcreteType shortArrType = new ConcreteArrayType(new ConcreteSimpleType(short.class));
    ConcreteType strALArrType =
        new ConcreteArrayType(
            ConcreteType.forClass(ArrayList.class, new ConcreteSimpleType(String.class)));
    ConcreteType intALArrType =
        new ConcreteArrayType(
            ConcreteType.forClass(ArrayList.class, new ConcreteSimpleType(Integer.class)));
    ConcreteType alArrType = new ConcreteArrayType(new ConcreteSimpleType(ArrayList.class));
    ConcreteType objArrType = new ConcreteArrayType(new ConcreteSimpleType(Object.class));
    ConcreteType intBoxArrType = new ConcreteArrayType(new ConcreteSimpleType(Integer.class));

    assertTrue("can assign array of same element type", intArrType.isAssignableFrom(intArrType));
    assertTrue(
        "can assign array of raw type to array of parameterized type",
        strALArrType.isAssignableFrom(alArrType));
    assertTrue("can assign Integer[] to Object[]", objArrType.isAssignableFrom(intBoxArrType));
    assertTrue(
        "can assign ArrayList<Integer>[] to Object[]", objArrType.isAssignableFrom(intALArrType));

    assertFalse(
        "cannot assign short array to int array", intArrType.isAssignableFrom(shortArrType));
    assertFalse(
        "cannot assign ArrayList<String> array to ArrayList<Integer> array",
        intALArrType.isAssignableFrom(strALArrType));
    assertFalse("cannot assign int array to Object array", objArrType.isAssignableFrom(intArrType));
  }

  @Test
  public void testNames() {
    ConcreteType intArrType = new ConcreteArrayType(new ConcreteSimpleType(int.class));
    ConcreteType strArrType = new ConcreteArrayType(new ConcreteSimpleType(String.class));
    ConcreteType intALArrType =
        new ConcreteArrayType(
            ConcreteType.forClass(ArrayList.class, new ConcreteSimpleType(Integer.class)));
    ConcreteType alArrType = new ConcreteArrayType(new ConcreteSimpleType(ArrayList.class));

    assertEquals("type name", "int[]", intArrType.getName());
    assertEquals("type name", "java.lang.String[]", strArrType.getName());
    assertEquals("type name", "java.util.ArrayList<java.lang.Integer>[]", intALArrType.getName());
    assertEquals("type name", "java.util.ArrayList[]", alArrType.getName());
  }

  @Test
  public void testConstructionFromHarvest() {
    Class<?> c = ArrayHarvest.class;

    Method m = null;
    Type t = null;
    randoop.types.GeneralType rt = null;

    try {
      m = c.getDeclaredMethod("genericArrayArg1");
    } catch (Exception e) {
      fail("could not get method");
    }
    t = m.getGenericReturnType();
    rt = randoop.types.GeneralType.forType(t);
    assertTrue("should be generic", rt.isGeneric());
    assertFalse("should not be an object", rt.isObject());

    try {
      m = c.getDeclaredMethod("genericArrayArg2");
    } catch (Exception e) {
      fail("could not get method");
    }
    t = m.getGenericReturnType();
    rt = randoop.types.GeneralType.forType(t);
    assertTrue("should be generic", rt.isGeneric());
    assertFalse("should not be an object", rt.isObject());

    try {
      m = c.getDeclaredMethod("concreteArrayArg1");
    } catch (Exception e) {
      fail("could not get method");
    }
    t = m.getGenericReturnType();
    rt = randoop.types.GeneralType.forType(t);
    assertTrue("should be generic", !rt.isGeneric());
    assertFalse("should not be an object", rt.isObject());

    try {
      m = c.getDeclaredMethod("concreteArrayArg2");
    } catch (Exception e) {
      fail("could not get method");
    }
    t = m.getGenericReturnType();
    rt = randoop.types.GeneralType.forType(t);
    assertTrue("should be generic", !rt.isGeneric());
    assertFalse("should not be an object", rt.isObject());
  }
}
