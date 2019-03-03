package randoop.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.ArrayList;
import org.junit.Test;

public class ArrayTypeTest {

  @Test
  public void testAssignability() {
    Type intArrType = ArrayType.ofComponentType(new PrimitiveType(int.class));
    Type shortArrType = ArrayType.ofComponentType(new PrimitiveType(short.class));
    Type strALArrType =
        ArrayType.ofComponentType(
            GenericClassType.forClass(ArrayList.class)
                .instantiate(new NonParameterizedType(String.class)));
    Type intALArrType =
        ArrayType.ofComponentType(
            GenericClassType.forClass(ArrayList.class)
                .instantiate(new NonParameterizedType(Integer.class)));
    Type alArrType = ArrayType.ofComponentType(new NonParameterizedType(ArrayList.class));
    Type objArrType = ArrayType.ofComponentType(new NonParameterizedType(Object.class));
    Type intBoxArrType = ArrayType.ofComponentType(new NonParameterizedType(Integer.class));

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
    Type intArrType = ArrayType.ofComponentType(new PrimitiveType(int.class));
    Type strArrType = ArrayType.ofComponentType(new NonParameterizedType(String.class));
    Type intALArrType =
        ArrayType.ofComponentType(
            GenericClassType.forClass(ArrayList.class)
                .instantiate(new NonParameterizedType(Integer.class)));
    Type alArrType = ArrayType.ofComponentType(new NonParameterizedType(ArrayList.class));

    assertEquals("type name", "int[]", intArrType.getName());
    assertEquals("type name", "java.lang.String[]", strArrType.getName());
    assertEquals("type name", "java.util.ArrayList<java.lang.Integer>[]", intALArrType.getName());
    assertEquals("type name", "java.util.ArrayList[]", alArrType.getName());
  }

  @Test
  public void testConstructionFromHarvest() throws NoSuchMethodException {
    Class<?> c = ArrayHarvest.class;

    Method m;
    java.lang.reflect.Type t;
    Type rt;

    m = c.getDeclaredMethod("genericArrayArg1");
    t = m.getGenericReturnType();
    rt = Type.forType(t);
    assertTrue("should be generic: " + rt, rt.isGeneric());
    assertFalse("should not be an object", rt.isObject());

    m = c.getDeclaredMethod("genericArrayArg2");
    t = m.getGenericReturnType();
    rt = Type.forType(t);
    assertTrue("should be generic: " + rt, rt.isGeneric());
    assertFalse("should not be an object", rt.isObject());

    m = c.getDeclaredMethod("concreteArrayArg1");
    t = m.getGenericReturnType();
    rt = Type.forType(t);
    assertTrue("should not be generic: " + rt, !rt.isGeneric());
    assertFalse("should not be an object", rt.isObject());

    m = c.getDeclaredMethod("concreteArrayArg2");
    t = m.getGenericReturnType();
    rt = Type.forType(t);
    assertTrue("should be generic: " + rt, !rt.isGeneric());
    assertFalse("should not be an object", rt.isObject());
  }
}
