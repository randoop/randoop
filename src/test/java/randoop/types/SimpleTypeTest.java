package randoop.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static randoop.types.ExampleClassesForTests.I;
import static randoop.types.ExampleClassesForTests.J;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.junit.Test;

/** Tests for the {@code SimpleType} class. */
public class SimpleTypeTest {

  /** Check that the methods do the obvious for a primitive type. */
  @Test
  public void testPrimitive() {
    Type intType = PrimitiveType.forClass(int.class);
    assertEquals("int", intType.getBinaryName());
    assertEquals(int.class, intType.getRuntimeClass());
    assertTrue(intType.runtimeClassIs(int.class));
    assertTrue(intType.isPrimitive());
    assertFalse(intType.isArray());
    assertFalse(intType.isVoid());
  }

  /**
   * Make sure that isAssignableFrom conforms to primitive widening relationship defined by JDK 7
   * JLS section 5.1.2
   */
  @Test
  public void testPrimitiveWidening() {
    Type booleanType = PrimitiveType.forClass(boolean.class);
    Type byteType = PrimitiveType.forClass(byte.class);
    Type charType = PrimitiveType.forClass(char.class);
    Type doubleType = PrimitiveType.forClass(double.class);
    Type floatType = PrimitiveType.forClass(float.class);
    Type intType = PrimitiveType.forClass(int.class);
    Type longType = PrimitiveType.forClass(long.class);
    Type shortType = PrimitiveType.forClass(short.class);

    // boolean to itself
    assertTrue(booleanType.isAssignableFrom(booleanType));
    // boolean to nothing else
    assertFalse(byteType.isAssignableFrom(booleanType));
    assertFalse(charType.isAssignableFrom(booleanType));
    assertFalse(doubleType.isAssignableFrom(booleanType));
    assertFalse(floatType.isAssignableFrom(booleanType));
    assertFalse(intType.isAssignableFrom(booleanType));
    assertFalse(longType.isAssignableFrom(booleanType));
    assertFalse(shortType.isAssignableFrom(booleanType));

    // byte to number types
    assertTrue(byteType.isAssignableFrom(byteType));
    assertTrue(shortType.isAssignableFrom(byteType));
    assertTrue(intType.isAssignableFrom(byteType));
    assertTrue(longType.isAssignableFrom(byteType));
    assertTrue(floatType.isAssignableFrom(byteType));
    assertTrue(doubleType.isAssignableFrom(byteType));
    // byte to nothing else
    assertFalse(booleanType.isAssignableFrom(byteType));
    assertFalse(charType.isAssignableFrom(byteType));

    // short to longer number types
    assertTrue(shortType.isAssignableFrom(shortType));
    assertTrue(intType.isAssignableFrom(shortType));
    assertTrue(longType.isAssignableFrom(shortType));
    assertTrue(floatType.isAssignableFrom(shortType));
    assertTrue(doubleType.isAssignableFrom(shortType));
    // short to nothing else
    assertFalse(booleanType.isAssignableFrom(shortType));
    assertFalse(byteType.isAssignableFrom(shortType));
    assertFalse(charType.isAssignableFrom(shortType));

    // char to int and longer number types
    assertTrue(charType.isAssignableFrom(charType));
    assertTrue(intType.isAssignableFrom(charType));
    assertTrue(longType.isAssignableFrom(charType));
    assertTrue(floatType.isAssignableFrom(charType));
    assertTrue(doubleType.isAssignableFrom(charType));
    // char to nothing else
    assertFalse(booleanType.isAssignableFrom(charType));
    assertFalse(byteType.isAssignableFrom(charType));
    assertFalse(shortType.isAssignableFrom(charType));

    // int to longer number types
    assertTrue(intType.isAssignableFrom(intType));
    assertTrue(longType.isAssignableFrom(intType));
    assertTrue(floatType.isAssignableFrom(intType));
    assertTrue(doubleType.isAssignableFrom(intType));
    // int to nothing else
    assertFalse(booleanType.isAssignableFrom(intType));
    assertFalse(byteType.isAssignableFrom(intType));
    assertFalse(charType.isAssignableFrom(intType));
    assertFalse(shortType.isAssignableFrom(intType));

    // long to floating point numbers
    assertTrue(longType.isAssignableFrom(longType));
    assertTrue(floatType.isAssignableFrom(longType));
    assertTrue(doubleType.isAssignableFrom(longType));
    // long to nothing else
    assertFalse(booleanType.isAssignableFrom(longType));
    assertFalse(byteType.isAssignableFrom(longType));
    assertFalse(charType.isAssignableFrom(longType));
    assertFalse(intType.isAssignableFrom(longType));
    assertFalse(shortType.isAssignableFrom(longType));

    // float to double
    assertTrue(floatType.isAssignableFrom(floatType));
    assertTrue(doubleType.isAssignableFrom(floatType));
    // float to nothing else
    assertFalse(booleanType.isAssignableFrom(floatType));
    assertFalse(byteType.isAssignableFrom(floatType));
    assertFalse(charType.isAssignableFrom(floatType));
    assertFalse(intType.isAssignableFrom(floatType));
    assertFalse(longType.isAssignableFrom(floatType));
    assertFalse(shortType.isAssignableFrom(floatType));

    // double to itself
    assertTrue(doubleType.isAssignableFrom(doubleType));
    // boolean to nothing else
    assertFalse(booleanType.isAssignableFrom(doubleType));
    assertFalse(byteType.isAssignableFrom(doubleType));
    assertFalse(charType.isAssignableFrom(doubleType));
    assertFalse(floatType.isAssignableFrom(doubleType));
    assertFalse(intType.isAssignableFrom(doubleType));
    assertFalse(longType.isAssignableFrom(doubleType));
    assertFalse(shortType.isAssignableFrom(doubleType));
  }

  /**
   * For some reason the names of types is a royal pain, though SimpleType names should be
   * straightforward. These are tests to make sure that what we are getting looks like what we
   * expect.
   */
  @Test
  public void testNames() throws ClassNotFoundException {
    Type t = NonParameterizedType.forClass(String.class);
    assertEquals("java.lang.String", t.getBinaryName());
    t = NonParameterizedType.forClass(randoop.types.test.Subclass.class);
    assertEquals("randoop.types.test.Subclass", t.getBinaryName());
    t = Type.forName("randoop.types.test.Subclass$Innerclass");
    assertEquals("randoop.types.test.Subclass$Innerclass", t.getBinaryName());
  }

  /**
   * void is a special case for assignment. Cannot take a value, so don't want any type to assign to
   * it, and don't want it to assign to any type (including itself).
   */
  @Test
  public void testVoidDoesNotConvert() {
    // Type voidType = PrimitiveType.forClass(void.class);
    Type voidType = JavaTypes.VOID_TYPE;
    Type objectType = NonParameterizedType.forClass(Object.class);
    Type booleanType = PrimitiveType.forClass(boolean.class);
    Type byteType = PrimitiveType.forClass(byte.class);
    Type charType = PrimitiveType.forClass(char.class);
    Type doubleType = PrimitiveType.forClass(double.class);
    Type floatType = PrimitiveType.forClass(float.class);
    Type intType = PrimitiveType.forClass(int.class);
    Type longType = PrimitiveType.forClass(long.class);
    Type shortType = PrimitiveType.forClass(short.class);

    assertTrue(void.class.isPrimitive());
    assertFalse(voidType.isAssignableFrom(voidType));
    assertFalse(voidType.isAssignableFrom(objectType));
    assertFalse(voidType.isAssignableFrom(booleanType));
    assertFalse(voidType.isAssignableFrom(byteType));
    assertFalse(voidType.isAssignableFrom(charType));
    assertFalse(voidType.isAssignableFrom(doubleType));
    assertFalse(voidType.isAssignableFrom(floatType));
    assertFalse(voidType.isAssignableFrom(intType));
    assertFalse(voidType.isAssignableFrom(longType));
    assertFalse(voidType.isAssignableFrom(shortType));

    assertFalse(objectType.isAssignableFrom(voidType));
    assertFalse(booleanType.isAssignableFrom(voidType));
    assertFalse(byteType.isAssignableFrom(voidType));
    assertFalse(charType.isAssignableFrom(voidType));
    assertFalse(doubleType.isAssignableFrom(voidType));
    assertFalse(intType.isAssignableFrom(voidType));
    assertFalse(floatType.isAssignableFrom(voidType));
    assertFalse(longType.isAssignableFrom(voidType));
    assertFalse(shortType.isAssignableFrom(voidType));
  }

  /** Object also a special case. Just want to make sure didn't mess up the obvious. */
  @Test
  public void testConversionsToObject() {
    Type objectType = NonParameterizedType.forClass(Object.class);
    Type booleanType = PrimitiveType.forClass(boolean.class);
    Type byteType = PrimitiveType.forClass(byte.class);
    Type charType = PrimitiveType.forClass(char.class);
    Type doubleType = PrimitiveType.forClass(double.class);
    Type floatType = PrimitiveType.forClass(float.class);
    Type intType = PrimitiveType.forClass(int.class);
    Type longType = PrimitiveType.forClass(long.class);
    Type shortType = PrimitiveType.forClass(short.class);
    Type subclassType = new NonParameterizedType(randoop.types.test.Subclass.class);
    Type intArrayType = ArrayType.ofComponentType(intType);
    Type intArrayListType =
        GenericClassType.forClass(ArrayList.class)
            .instantiate(ReferenceType.forClass(Integer.class));

    assertTrue(objectType.isAssignableFrom(objectType));
    assertTrue(objectType.isAssignableFrom(booleanType));
    assertTrue(objectType.isAssignableFrom(byteType));
    assertTrue(objectType.isAssignableFrom(charType));
    assertTrue(objectType.isAssignableFrom(doubleType));
    assertTrue(objectType.isAssignableFrom(floatType));
    assertTrue(objectType.isAssignableFrom(intType));
    assertTrue(objectType.isAssignableFrom(longType));
    assertTrue(objectType.isAssignableFrom(shortType));
    assertTrue(objectType.isAssignableFrom(subclassType));
    assertTrue(objectType.isAssignableFrom(intArrayType));
    assertTrue(objectType.isAssignableFrom(intArrayListType));
  }

  /** Make sure boxing/unboxing conversions work in assignment. */
  @Test
  public void testBoxingUnboxingConversions() {
    Type booleanType = PrimitiveType.forClass(boolean.class);
    Type boxedBooleanType = NonParameterizedType.forClass(Boolean.class);
    assertTrue(booleanType.isAssignableFrom(boxedBooleanType));
    assertTrue(boxedBooleanType.isAssignableFrom(booleanType));

    Type byteType = PrimitiveType.forClass(byte.class);
    Type boxedByteType = NonParameterizedType.forClass(Byte.class);
    assertTrue(byteType.isAssignableFrom(boxedByteType));
    assertTrue(boxedByteType.isAssignableFrom(byteType));

    Type charType = PrimitiveType.forClass(char.class);
    Type boxedCharType = NonParameterizedType.forClass(Character.class);
    assertTrue(charType.isAssignableFrom(boxedCharType));
    assertTrue(boxedCharType.isAssignableFrom(charType));

    Type doubleType = PrimitiveType.forClass(double.class);
    Type boxedDoubleType = NonParameterizedType.forClass(Double.class);
    assertTrue(doubleType.isAssignableFrom(boxedDoubleType));
    assertTrue(boxedDoubleType.isAssignableFrom(doubleType));

    Type floatType = PrimitiveType.forClass(float.class);
    Type boxedfloatType = NonParameterizedType.forClass(Float.class);
    assertTrue(floatType.isAssignableFrom(boxedfloatType));
    assertTrue(boxedfloatType.isAssignableFrom(floatType));

    Type intType = PrimitiveType.forClass(int.class);
    Type boxedIntType = NonParameterizedType.forClass(Integer.class);
    assertTrue(intType.isAssignableFrom(boxedIntType));
    assertTrue(boxedIntType.isAssignableFrom(intType));

    Type longType = PrimitiveType.forClass(long.class);
    Type boxedLongType = NonParameterizedType.forClass(Long.class);
    assertTrue(longType.isAssignableFrom(boxedLongType));
    assertTrue(boxedLongType.isAssignableFrom(longType));

    Type shortType = PrimitiveType.forClass(short.class);
    Type boxedShortType = NonParameterizedType.forClass(Short.class);
    assertTrue(shortType.isAssignableFrom(boxedShortType));
    assertTrue(boxedShortType.isAssignableFrom(shortType));
    assertFalse(boxedIntType.isAssignableFrom(shortType));
    assertTrue(intType.isAssignableFrom(boxedShortType));
  }

  @Test
  public void testRawtypeAssignability() {
    Type rawALType = NonParameterizedType.forClass(ArrayList.class);
    Type parameterizedALType =
        GenericClassType.forClass(ArrayList.class)
            .instantiate(NonParameterizedType.forClass(String.class));
    assertTrue(rawALType.isAssignableFrom(parameterizedALType));

    Type rawCollType = NonParameterizedType.forClass(Collection.class);
    assertTrue(rawCollType.isAssignableFrom(parameterizedALType));
    Type rawSetType = NonParameterizedType.forClass(Set.class);
    assertFalse(rawSetType.isAssignableFrom(parameterizedALType));
  }

  @Test
  public void testExtendingGeneric() {
    // class I {}
    // class J<T> extends I {}
    Type iType = Type.forClass(I.class);
    Type strJType =
        GenericClassType.forClass(J.class).instantiate(NonParameterizedType.forClass(String.class));
    assertTrue(iType.isAssignableFrom(strJType));
  }

  @Test
  public void testIsRawtype() {
    List<Type> typeList = new ArrayList<>();
    typeList.add(Type.forClass(Date.class));
    typeList.add(Type.forClass(Integer.class));
    typeList.add(Type.forClass(Object.class));
    for (Type t : typeList) {
      assertFalse("type " + t + " shouldn't be rawtype", t.isRawtype());
    }
  }
}
