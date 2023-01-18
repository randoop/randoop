package randoop.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Array;
import java.util.ArrayList;
import org.junit.Test;

public class ConcreteTypeTest {

  @Test
  public void testForClass() {
    Type primitiveType = Type.forClass(int.class);
    assertEquals(PrimitiveType.forClass(int.class), primitiveType);
    assertTrue(primitiveType.isPrimitive());
    assertFalse(primitiveType.isGeneric());
    assertFalse(primitiveType.isArray());
    assertFalse(primitiveType.isBoxedPrimitive());
    assertFalse(primitiveType.isEnum());
    assertFalse(primitiveType.isInterface());
    assertFalse(primitiveType.isObject());
    assertFalse(primitiveType.isParameterized());
    assertFalse(primitiveType.isRawtype());
    assertFalse(primitiveType.isReferenceType());
    assertFalse(primitiveType.isString());
    assertFalse(primitiveType.isVoid());

    Type classType = Type.forClass(String.class);
    assertEquals(new NonParameterizedType(String.class), classType);
    assertFalse(classType.isPrimitive());
    assertFalse(classType.isGeneric());
    assertFalse(classType.isArray());
    assertFalse(classType.isBoxedPrimitive());
    assertFalse(classType.isEnum());
    assertFalse(classType.isInterface());
    assertFalse(classType.isObject());
    assertFalse(classType.isParameterized());
    assertFalse(classType.isRawtype());
    assertTrue(classType.isReferenceType());
    assertTrue(classType.isString());
    assertFalse(classType.isVoid());

    Class<?> arrayClass = Array.newInstance(String.class, 0).getClass();
    Type arrayType = Type.forClass(arrayClass);
    assertEquals(ArrayType.ofComponentType(new NonParameterizedType(String.class)), arrayType);
    assertFalse(arrayType.isPrimitive());
    assertFalse(arrayType.isGeneric());
    assertTrue(arrayType.isArray());
    assertFalse(arrayType.isBoxedPrimitive());
    assertFalse(arrayType.isEnum());
    assertFalse(arrayType.isInterface());
    assertFalse(arrayType.isObject());
    assertFalse(arrayType.isParameterized());
    assertFalse(arrayType.isRawtype());
    assertTrue(arrayType.isReferenceType());
    assertFalse(arrayType.isString());
    assertFalse(arrayType.isVoid());

    Type rawClassType = new NonParameterizedType(ArrayList.class);
    assertEquals(new NonParameterizedType(ArrayList.class), rawClassType);
    assertFalse(rawClassType.isPrimitive());
    assertFalse(rawClassType.isGeneric());
    assertFalse(rawClassType.isArray());
    assertFalse(rawClassType.isBoxedPrimitive());
    assertFalse(rawClassType.isEnum());
    assertFalse(rawClassType.isInterface());
    assertFalse(rawClassType.isObject());
    assertFalse(rawClassType.isParameterized());
    assertTrue(rawClassType.isRawtype());
    assertTrue(rawClassType.isReferenceType());
    assertFalse(rawClassType.isString());
    assertFalse(rawClassType.isVoid());
  }

  @Test
  public void testClassType() {
    Type classType = Type.forClass(Class.class);
    assertFalse(classType.isPrimitive());
    assertTrue(classType.isGeneric());
    assertFalse(classType.isArray());
    assertFalse(classType.isBoxedPrimitive());
    assertFalse(classType.isEnum());
    assertFalse(classType.isInterface());
    assertFalse(classType.isObject());
    assertFalse(classType.isParameterized());
    assertFalse(classType.isRawtype());
    assertTrue(classType.isReferenceType());
    assertFalse(classType.isString());
    assertFalse(classType.isVoid());
  }
}
