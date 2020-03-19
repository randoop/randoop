package randoop.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static randoop.types.ExampleClassesForTests.ArrayHarvest;

import java.lang.reflect.Method;
import java.util.ArrayList;
import org.junit.Assert;
import org.junit.Test;

public class ArrayTypeTest {

  @Test
  public void testAssignability() {
    Type intArrType = ArrayType.ofComponentType(PrimitiveType.forClass(int.class));
    Type shortArrType = ArrayType.ofComponentType(PrimitiveType.forClass(short.class));
    Type strALArrType =
        ArrayType.ofComponentType(
            GenericClassType.forClass(ArrayList.class)
                .instantiate(NonParameterizedType.forClass(String.class)));
    Type intALArrType =
        ArrayType.ofComponentType(
            GenericClassType.forClass(ArrayList.class)
                .instantiate(NonParameterizedType.forClass(Integer.class)));
    Type alArrType = ArrayType.ofComponentType(NonParameterizedType.forClass(ArrayList.class));
    Type objArrType = ArrayType.ofComponentType(NonParameterizedType.forClass(Object.class));
    Type intBoxArrType = ArrayType.ofComponentType(NonParameterizedType.forClass(Integer.class));

    assertTrue(intArrType.isAssignableFrom(intArrType));
    assertTrue(strALArrType.isAssignableFrom(alArrType));
    assertTrue(objArrType.isAssignableFrom(intBoxArrType));
    assertTrue(objArrType.isAssignableFrom(intALArrType));

    assertFalse(intArrType.isAssignableFrom(shortArrType));
    assertFalse(intALArrType.isAssignableFrom(strALArrType));
    assertFalse(objArrType.isAssignableFrom(intArrType));
  }

  @Test
  public void testNames() {
    Type intArrType = ArrayType.ofComponentType(PrimitiveType.forClass(int.class));
    Type strArrType = ArrayType.ofComponentType(NonParameterizedType.forClass(String.class));
    Type intALArrType =
        ArrayType.ofComponentType(
            GenericClassType.forClass(ArrayList.class)
                .instantiate(NonParameterizedType.forClass(Integer.class)));
    Type alArrType = ArrayType.ofComponentType(NonParameterizedType.forClass(ArrayList.class));

    assertEquals("int[]", intArrType.getBinaryName());
    assertEquals("java.lang.String[]", strArrType.getBinaryName());
    assertEquals("java.util.ArrayList<java.lang.Integer>[]", intALArrType.getBinaryName());
    assertEquals("java.util.ArrayList[]", alArrType.getBinaryName());
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
    assertFalse(rt.isObject());

    m = c.getDeclaredMethod("genericArrayArg2");
    t = m.getGenericReturnType();
    rt = Type.forType(t);
    assertTrue("should be generic: " + rt, rt.isGeneric());
    assertFalse(rt.isObject());

    m = c.getDeclaredMethod("concreteArrayArg1");
    t = m.getGenericReturnType();
    rt = Type.forType(t);
    assertTrue("should not be generic: " + rt, !rt.isGeneric());
    assertFalse(rt.isObject());

    m = c.getDeclaredMethod("concreteArrayArg2");
    t = m.getGenericReturnType();
    rt = Type.forType(t);
    assertTrue("should be generic: " + rt, !rt.isGeneric());
    assertFalse(rt.isObject());
  }

  @Test
  public void testFullyQualifiedArrayParsing() {
    String objectArraySignature = "java.lang.Object[]";
    String multiDimObjectArraySignature = "java.lang.Object[][][][][][][]";
    String primitiveArraySignature = "int[]";
    String nonArraySignature = "java.lang.Object";
    String primitiveNonArraySignature = "double";

    // Following should be parsed as 'java.util.Formatter$BigDecimalLayoutForm'
    String innerClassArraySignature = "java.util.Formatter$BigDecimalLayoutForm[][]";
    String innerClassNonArraySignature = "java.util.Formatter$BigDecimalLayoutForm";

    try {
      assertTrue(Type.getTypeforFullyQualifiedName(objectArraySignature) instanceof ArrayType);
      assertFalse(Type.forFullyQualifiedName(objectArraySignature) == null);

      assertTrue(
          Type.getTypeforFullyQualifiedName(multiDimObjectArraySignature) instanceof ArrayType);
      assertFalse(Type.forFullyQualifiedName(multiDimObjectArraySignature) == null);

      assertTrue(Type.getTypeforFullyQualifiedName(primitiveArraySignature) instanceof ArrayType);
      assertFalse(Type.forFullyQualifiedName(primitiveArraySignature) == null);

      assertFalse(Type.getTypeforFullyQualifiedName(nonArraySignature) instanceof ArrayType);
      assertFalse(Type.forFullyQualifiedName(nonArraySignature) == null);

      assertFalse(
          Type.getTypeforFullyQualifiedName(primitiveNonArraySignature) instanceof ArrayType);
      assertFalse(Type.forFullyQualifiedName(primitiveNonArraySignature) == null);

      assertTrue(Type.getTypeforFullyQualifiedName(innerClassArraySignature) instanceof ArrayType);
      assertFalse(Type.forFullyQualifiedName(innerClassArraySignature) == null);

      assertFalse(
          Type.getTypeforFullyQualifiedName(innerClassNonArraySignature) instanceof ArrayType);
      assertFalse(Type.forFullyQualifiedName(innerClassNonArraySignature) == null);
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
      Assert.fail();
    }

    String invalidArrayInnerClass = "java.util.Formatter$IDontExist[][]";
    try {
      Type.forFullyQualifiedName(invalidArrayInnerClass);
      Assert.fail();
    } catch (ClassNotFoundException e) {
      // Good
    }
    try {
      Type.getTypeforFullyQualifiedName(invalidArrayInnerClass);
      Assert.fail();
    } catch (ClassNotFoundException e) {
      // Good
    }
  }
}
