package randoop.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
    Type intType = new PrimitiveType(int.class);
    assertEquals("name of int is int", "int", intType.getName());
    assertEquals("runtime class of int type is int.class", int.class, intType.getRuntimeClass());
    assertTrue("int type has runtime class of int.class", intType.runtimeClassIs(int.class));
    assertTrue("int type is primitive", intType.isPrimitive());
    assertFalse("int type is not array", intType.isArray());
    assertFalse("int type is not void", intType.isVoid());
  }

  /**
   * Make sure that isAssignableFrom conforms to primitive widening relationship defined by JDK 7
   * JLS section 5.1.2
   */
  @Test
  public void testPrimitiveWidening() {
    Type booleanType = new PrimitiveType(boolean.class);
    Type byteType = new PrimitiveType(byte.class);
    Type charType = new PrimitiveType(char.class);
    Type doubleType = new PrimitiveType(double.class);
    Type floatType = new PrimitiveType(float.class);
    Type intType = new PrimitiveType(int.class);
    Type longType = new PrimitiveType(long.class);
    Type shortType = new PrimitiveType(short.class);

    // boolean to itself
    assertTrue("boolean is assignable from boolean", booleanType.isAssignableFrom(booleanType));
    // boolean to nothing else
    assertFalse("byte is not assignable from boolean", byteType.isAssignableFrom(booleanType));
    assertFalse("char is not assignable from boolean", charType.isAssignableFrom(booleanType));
    assertFalse("double is not assignable from boolean", doubleType.isAssignableFrom(booleanType));
    assertFalse("float is not assignable from boolean", floatType.isAssignableFrom(booleanType));
    assertFalse("int is not assignable from boolean", intType.isAssignableFrom(booleanType));
    assertFalse("long is not assignable from boolean", longType.isAssignableFrom(booleanType));
    assertFalse("short is not assignable from boolean", shortType.isAssignableFrom(booleanType));

    // byte to number types
    assertTrue("byte is assignable from byte", byteType.isAssignableFrom(byteType));
    assertTrue("short is assignable from byte", shortType.isAssignableFrom(byteType));
    assertTrue("int is assignable from byte", intType.isAssignableFrom(byteType));
    assertTrue("long is assignable from byte", longType.isAssignableFrom(byteType));
    assertTrue("float is assignable from byte", floatType.isAssignableFrom(byteType));
    assertTrue("double is assignable from byte", doubleType.isAssignableFrom(byteType));
    // byte to nothing else
    assertFalse("boolean is not assignable from byte", booleanType.isAssignableFrom(byteType));
    assertFalse("char is not assignable from byte", charType.isAssignableFrom(byteType));

    // short to longer number types
    assertTrue("short is assignable from short", shortType.isAssignableFrom(shortType));
    assertTrue("int is assignable from short", intType.isAssignableFrom(shortType));
    assertTrue("long is assignable from short", longType.isAssignableFrom(shortType));
    assertTrue("float is assignable from short", floatType.isAssignableFrom(shortType));
    assertTrue("double is assignable from short", doubleType.isAssignableFrom(shortType));
    // short to nothing else
    assertFalse("boolean is not assignable from short", booleanType.isAssignableFrom(shortType));
    assertFalse("byte is not assignable from short", byteType.isAssignableFrom(shortType));
    assertFalse("char is not assignable from short", charType.isAssignableFrom(shortType));

    // char to int and longer number types
    assertTrue("char is assignable from char", charType.isAssignableFrom(charType));
    assertTrue("int is assignable from char", intType.isAssignableFrom(charType));
    assertTrue("long is assignable from char", longType.isAssignableFrom(charType));
    assertTrue("float is assignable from char", floatType.isAssignableFrom(charType));
    assertTrue("double is assignable from char", doubleType.isAssignableFrom(charType));
    // char to nothing else
    assertFalse("boolean is not assignable from char", booleanType.isAssignableFrom(charType));
    assertFalse("byte is not assignable from char", byteType.isAssignableFrom(charType));
    assertFalse("short is not assignable from char", shortType.isAssignableFrom(charType));

    // int to longer number types
    assertTrue("int is assignable from int", intType.isAssignableFrom(intType));
    assertTrue("long is assignable from int", longType.isAssignableFrom(intType));
    assertTrue("float is assignable from int", floatType.isAssignableFrom(intType));
    assertTrue("double is assignable from int", doubleType.isAssignableFrom(intType));
    // int to nothing else
    assertFalse("boolean is not assignable from int", booleanType.isAssignableFrom(intType));
    assertFalse("byte is not assignable from int", byteType.isAssignableFrom(intType));
    assertFalse("char is not assignable from int", charType.isAssignableFrom(intType));
    assertFalse("short is not assignable from int", shortType.isAssignableFrom(intType));

    // long to floating point numbers
    assertTrue("long is assignable from long", longType.isAssignableFrom(longType));
    assertTrue("float is assignable from long", floatType.isAssignableFrom(longType));
    assertTrue("double is assignable from long", doubleType.isAssignableFrom(longType));
    // long to nothing else
    assertFalse("boolean is not assignable from long", booleanType.isAssignableFrom(longType));
    assertFalse("byte is not assignable from long", byteType.isAssignableFrom(longType));
    assertFalse("char is not assignable from long", charType.isAssignableFrom(longType));
    assertFalse("int is not assignable from long", intType.isAssignableFrom(longType));
    assertFalse("short is not assignable from long", shortType.isAssignableFrom(longType));

    // float to double
    assertTrue("float is assignable from float", floatType.isAssignableFrom(floatType));
    assertTrue("double is assignable from float", doubleType.isAssignableFrom(floatType));
    // float to nothing else
    assertFalse("boolean is not assignable from float", booleanType.isAssignableFrom(floatType));
    assertFalse("byte is not assignable from float", byteType.isAssignableFrom(floatType));
    assertFalse("char is not assignable from float", charType.isAssignableFrom(floatType));
    assertFalse("int is not assignable from float", intType.isAssignableFrom(floatType));
    assertFalse("long is not assignable from float", longType.isAssignableFrom(floatType));
    assertFalse("short is not assignable from float", shortType.isAssignableFrom(floatType));

    // double to itself
    assertTrue("double is assignable from double", doubleType.isAssignableFrom(doubleType));
    // boolean to nothing else
    assertFalse("boolean is not assignable from double", booleanType.isAssignableFrom(doubleType));
    assertFalse("byte is not assignable from double", byteType.isAssignableFrom(doubleType));
    assertFalse("char is not assignable from double", charType.isAssignableFrom(doubleType));
    assertFalse("float is not assignable from double", floatType.isAssignableFrom(doubleType));
    assertFalse("int is not assignable from double", intType.isAssignableFrom(doubleType));
    assertFalse("long is not assignable from double", longType.isAssignableFrom(doubleType));
    assertFalse("short is not assignable from double", shortType.isAssignableFrom(doubleType));
  }

  /**
   * For some reason the names of types is a royal pain, though SimpleType names should be
   * straightforward. These are tests to make sure that what we are getting looks like what we
   * expect.
   */
  @Test
  public void testNames() throws ClassNotFoundException {
    Type t = new NonParameterizedType(String.class);
    assertEquals("name should match", "java.lang.String", t.getName());
    t = new NonParameterizedType(randoop.types.test.Subclass.class);
    assertEquals("name should match", "randoop.types.test.Subclass", t.getName());
    t = Type.forName("randoop.types.test.Subclass$Innerclass");
    assertEquals("name should match", "randoop.types.test.Subclass.Innerclass", t.getName());
  }

  /**
   * void is a special case for assignment. Cannot take a value, so don't want any type to assign to
   * it, and don't want it to assign to any type (including itself).
   */
  @Test
  public void testVoidDoesNotConvert() {
    // Type voidType = new PrimitiveType(void.class);
    Type voidType = JavaTypes.VOID_TYPE;
    Type objectType = new NonParameterizedType(Object.class);
    Type booleanType = new PrimitiveType(boolean.class);
    Type byteType = new PrimitiveType(byte.class);
    Type charType = new PrimitiveType(char.class);
    Type doubleType = new PrimitiveType(double.class);
    Type floatType = new PrimitiveType(float.class);
    Type intType = new PrimitiveType(int.class);
    Type longType = new PrimitiveType(long.class);
    Type shortType = new PrimitiveType(short.class);

    assertTrue("void is primitive", void.class.isPrimitive());
    assertFalse("void is not assignable from void", voidType.isAssignableFrom(voidType));
    assertFalse("void is not assignable from Object", voidType.isAssignableFrom(objectType));
    assertFalse("void is not assignable from boolean", voidType.isAssignableFrom(booleanType));
    assertFalse("void is not assignable from byte", voidType.isAssignableFrom(byteType));
    assertFalse("void is not assignable from char", voidType.isAssignableFrom(charType));
    assertFalse("void is not assignable from double", voidType.isAssignableFrom(doubleType));
    assertFalse("void is not assignable from float", voidType.isAssignableFrom(floatType));
    assertFalse("void is not assignable from int", voidType.isAssignableFrom(intType));
    assertFalse("void is not assignable from long", voidType.isAssignableFrom(longType));
    assertFalse("void is not assignable from short", voidType.isAssignableFrom(shortType));

    assertFalse("void is not assignable from Object", objectType.isAssignableFrom(voidType));
    assertFalse("boolean is not assignable from void", booleanType.isAssignableFrom(voidType));
    assertFalse("byte is not assignable from void", byteType.isAssignableFrom(voidType));
    assertFalse("char is not assignable from void", charType.isAssignableFrom(voidType));
    assertFalse("double is not assignable from void", doubleType.isAssignableFrom(voidType));
    assertFalse("int is not assignable from void", intType.isAssignableFrom(voidType));
    assertFalse("float is not assignable from void", floatType.isAssignableFrom(voidType));
    assertFalse("long is not assignable from void", longType.isAssignableFrom(voidType));
    assertFalse("short is not assignable from void", shortType.isAssignableFrom(voidType));
  }

  /** Object also a special case. Just want to make sure didn't mess up the obvious. */
  @Test
  public void testConversionsToObject() {
    Type objectType = new NonParameterizedType(Object.class);
    Type booleanType = new PrimitiveType(boolean.class);
    Type byteType = new PrimitiveType(byte.class);
    Type charType = new PrimitiveType(char.class);
    Type doubleType = new PrimitiveType(double.class);
    Type floatType = new PrimitiveType(float.class);
    Type intType = new PrimitiveType(int.class);
    Type longType = new PrimitiveType(long.class);
    Type shortType = new PrimitiveType(short.class);
    Type subclassType = new NonParameterizedType(randoop.types.test.Subclass.class);
    Type intArrayType = ArrayType.ofComponentType(intType);
    Type intArrayListType =
        GenericClassType.forClass(ArrayList.class)
            .instantiate(ReferenceType.forClass(Integer.class));

    assertTrue("Object is assignable from all types", objectType.isAssignableFrom(objectType));
    assertTrue("Object is assignable from all types", objectType.isAssignableFrom(booleanType));
    assertTrue("Object is assignable from all types", objectType.isAssignableFrom(byteType));
    assertTrue("Object is assignable from all types", objectType.isAssignableFrom(charType));
    assertTrue("Object is assignable from all types", objectType.isAssignableFrom(doubleType));
    assertTrue("Object is assignable from all types", objectType.isAssignableFrom(floatType));
    assertTrue("Object is assignable from all types", objectType.isAssignableFrom(intType));
    assertTrue("Object is assignable from all types", objectType.isAssignableFrom(longType));
    assertTrue("Object is assignable from all types", objectType.isAssignableFrom(shortType));
    assertTrue("Object is assignable from all types", objectType.isAssignableFrom(subclassType));
    assertTrue("Object is assignable from all types", objectType.isAssignableFrom(intArrayType));
    assertTrue(
        "Object is assignable from all types", objectType.isAssignableFrom(intArrayListType));
  }

  /** Make sure boxing/unboxing conversions work in assignment. */
  @Test
  public void testBoxingUnboxingConversions() {
    Type booleanType = new PrimitiveType(boolean.class);
    Type boxedBooleanType = new NonParameterizedType(Boolean.class);
    assertTrue("boolean assignable from boxed", booleanType.isAssignableFrom(boxedBooleanType));
    assertTrue(
        "boxed boolean assignable from unboxed", boxedBooleanType.isAssignableFrom(booleanType));

    Type byteType = new PrimitiveType(byte.class);
    Type boxedByteType = new NonParameterizedType(Byte.class);
    assertTrue("byte assignable from boxed", byteType.isAssignableFrom(boxedByteType));
    assertTrue("boxed byte assignable from unboxed", boxedByteType.isAssignableFrom(byteType));

    Type charType = new PrimitiveType(char.class);
    Type boxedCharType = new NonParameterizedType(Character.class);
    assertTrue("char assignable from boxed", charType.isAssignableFrom(boxedCharType));
    assertTrue("boxed char assignable from unboxed", boxedCharType.isAssignableFrom(charType));

    Type doubleType = new PrimitiveType(double.class);
    Type boxedDoubleType = new NonParameterizedType(Double.class);
    assertTrue("double assignable from boxed", doubleType.isAssignableFrom(boxedDoubleType));
    assertTrue(
        "boxed double assignable from unboxed", boxedDoubleType.isAssignableFrom(doubleType));

    Type floatType = new PrimitiveType(float.class);
    Type boxedfloatType = new NonParameterizedType(Float.class);
    assertTrue("float assignable from boxed", floatType.isAssignableFrom(boxedfloatType));
    assertTrue("boxed float assignable from unboxed", boxedfloatType.isAssignableFrom(floatType));

    Type intType = new PrimitiveType(int.class);
    Type boxedIntType = new NonParameterizedType(Integer.class);
    assertTrue("int assignable from boxed", intType.isAssignableFrom(boxedIntType));
    assertTrue("boxed int assignable from unboxed", boxedIntType.isAssignableFrom(intType));

    Type longType = new PrimitiveType(long.class);
    Type boxedLongType = new NonParameterizedType(Long.class);
    assertTrue("long assignable from boxed", longType.isAssignableFrom(boxedLongType));
    assertTrue("boxed long assignable from unboxed", boxedLongType.isAssignableFrom(longType));

    Type shortType = new PrimitiveType(short.class);
    Type boxedShortType = new NonParameterizedType(Short.class);
    assertTrue("short assignable from boxed", shortType.isAssignableFrom(boxedShortType));
    assertTrue("boxed short assignable from unboxed", boxedShortType.isAssignableFrom(shortType));
    assertFalse("boxed int not assignable from short", boxedIntType.isAssignableFrom(shortType));
    assertTrue("int assignable from boxed short", intType.isAssignableFrom(boxedShortType));
  }

  @Test
  public void testRawtypeAssignability() {
    Type rawALType = new NonParameterizedType(ArrayList.class);
    Type parameterizedALType =
        GenericClassType.forClass(ArrayList.class)
            .instantiate(new NonParameterizedType(String.class));
    assertTrue(
        "ArrayList is assignable from ArrayList<String>",
        rawALType.isAssignableFrom(parameterizedALType));

    Type rawCollType = new NonParameterizedType(Collection.class);
    assertTrue(
        "Collection is assignable from ArrayList<String>",
        rawCollType.isAssignableFrom(parameterizedALType));
    Type rawSetType = new NonParameterizedType(Set.class);
    assertFalse(
        "Set is not assignable from ArrayList<String>",
        rawSetType.isAssignableFrom(parameterizedALType));
  }

  @Test
  public void testExtendingGeneric() {
    // class I {}
    // class J<T> extends I {}
    Type iType = Type.forClass(I.class);
    Type strJType =
        GenericClassType.forClass(J.class).instantiate(new NonParameterizedType(String.class));
    assertTrue("J<String> is assignable to I", iType.isAssignableFrom(strJType));
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
