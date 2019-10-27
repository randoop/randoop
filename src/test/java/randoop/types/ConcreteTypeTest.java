package randoop.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Array;
import java.util.ArrayList;
import org.junit.Test;

public class ConcreteTypeTest {

  @Test
  public void testForClass() {
    Type primitiveType = Type.forClass(int.class);
    assertEquals("builds primitive correctly", PrimitiveType.forClass(int.class), primitiveType);
    assertTrue("is primitive", primitiveType.isPrimitive());
    assertTrue("is not generic", !primitiveType.isGeneric());
    assertTrue("is not array", !primitiveType.isArray());
    assertTrue("is not boxed primitive", !primitiveType.isBoxedPrimitive());
    assertTrue("is not enum", !primitiveType.isEnum());
    assertTrue("is not interface", !primitiveType.isInterface());
    assertTrue("is not Object", !primitiveType.isObject());
    assertTrue("is not Parameterized", !primitiveType.isParameterized());
    assertTrue("is not rawtype", !primitiveType.isRawtype());
    assertTrue("is not reference type", !primitiveType.isReferenceType());
    assertTrue("is not String", !primitiveType.isString());
    assertTrue("is not void", !primitiveType.isVoid());

    Type classType = Type.forClass(String.class);
    assertEquals("builds class type correctly", new NonParameterizedType(String.class), classType);
    assertTrue("is not primitive", !classType.isPrimitive());
    assertTrue("is not generic", !classType.isGeneric());
    assertTrue("is not array", !classType.isArray());
    assertTrue("is not boxed primitive", !classType.isBoxedPrimitive());
    assertTrue("is not enum", !classType.isEnum());
    assertTrue("is not interface", !classType.isInterface());
    assertTrue("is not Object", !classType.isObject());
    assertTrue("is not Parameterized", !classType.isParameterized());
    assertTrue("is not rawtype", !classType.isRawtype());
    assertTrue("is reference type", classType.isReferenceType());
    assertTrue("is String", classType.isString());
    assertTrue("is not void", !classType.isVoid());

    Class<?> arrayClass = Array.newInstance(String.class, 0).getClass();
    Type arrayType = Type.forClass(arrayClass);
    assertEquals(
        "builds array type correctly",
        ArrayType.ofComponentType(new NonParameterizedType(String.class)),
        arrayType);
    assertTrue("is not primitive", !arrayType.isPrimitive());
    assertTrue("is not generic", !arrayType.isGeneric());
    assertTrue("is array", arrayType.isArray());
    assertTrue("is not boxed primitive", !arrayType.isBoxedPrimitive());
    assertTrue("is not enum", !arrayType.isEnum());
    assertTrue("is not interface", !arrayType.isInterface());
    assertTrue("is not Object", !arrayType.isObject());
    assertTrue("is not Parameterized", !arrayType.isParameterized());
    assertTrue("is not rawtype", !arrayType.isRawtype());
    assertTrue("is reference type", arrayType.isReferenceType());
    assertTrue("is not String", !arrayType.isString());
    assertTrue("is not void", !arrayType.isVoid());

    Type rawClassType = new NonParameterizedType(ArrayList.class);
    assertEquals(
        "builds raw class type correctly", new NonParameterizedType(ArrayList.class), rawClassType);
    assertTrue("is not primitive", !rawClassType.isPrimitive());
    assertTrue("is not generic", !rawClassType.isGeneric());
    assertTrue("is not array", !rawClassType.isArray());
    assertTrue("is not boxed primitive", !rawClassType.isBoxedPrimitive());
    assertTrue("is not enum", !rawClassType.isEnum());
    assertTrue("is not interface", !rawClassType.isInterface());
    assertTrue("is not Object", !rawClassType.isObject());
    assertTrue("is not Parameterized", !rawClassType.isParameterized());
    assertTrue("is rawtype", rawClassType.isRawtype());
    assertTrue("is reference type", rawClassType.isReferenceType());
    assertTrue("is not String", !rawClassType.isString());
    assertTrue("is not void", !rawClassType.isVoid());
  }

  @Test
  public void testClassType() {
    Type classType = Type.forClass(Class.class);
    assertTrue("is not primitive", !classType.isPrimitive());
    assertTrue("is generic", classType.isGeneric());
    assertTrue("is not array", !classType.isArray());
    assertTrue("is not boxed primitive", !classType.isBoxedPrimitive());
    assertTrue("is not enum", !classType.isEnum());
    assertTrue("is not interface", !classType.isInterface());
    assertTrue("is not Object", !classType.isObject());
    assertTrue("is not Parameterized", !classType.isParameterized());
    assertTrue("is not rawtype", !classType.isRawtype());
    assertTrue("is reference type", classType.isReferenceType());
    assertTrue("is not String", !classType.isString());
    assertTrue("is not void", !classType.isVoid());
  }
}
