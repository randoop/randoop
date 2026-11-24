package randoop.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static randoop.types.ExampleClassesForTests.ArrayHarvest;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
    } catch (ClassNotFoundException | NoClassDefFoundError e) {
      e.printStackTrace(System.out);
      Assert.fail();
    }

    String invalidArrayInnerClass = "java.util.Formatter$IDontExist[][]";
    try {
      Type.forFullyQualifiedName(invalidArrayInnerClass);
      Assert.fail();
    } catch (ClassNotFoundException | NoClassDefFoundError e) {
      // Good
    }
    try {
      Type.getTypeforFullyQualifiedName(invalidArrayInnerClass);
      Assert.fail();
    } catch (ClassNotFoundException | NoClassDefFoundError e) {
      // Good
    }
  }

  /**
   * A class whose field has an array of the type parameter as its type.
   *
   * @param <T> the type parameter
   */
  @SuppressWarnings("unused")
  private static class ArrayOfTypeParameter<T> {
    /** A field whose type is an array of the type parameter. */
    T[] array;
  }

  @Test
  public void testSubstituteNullTypeForComponent() throws NoSuchFieldException {
    ArrayType arrayType =
        ArrayType.forType(ArrayOfTypeParameter.class.getDeclaredField("array").getGenericType());
    TypeVariable t = (TypeVariable) arrayType.getComponentType();
    ArrayType nullArray = arrayType.substitute(new Substitution(t, JavaTypes.NULL_TYPE));
    assertEquals(JavaTypes.NULL_TYPE, nullArray.getComponentType());
    // There is no array class of the null type, so the runtime class is Object[].
    assertEquals(Object[].class, nullArray.getRuntimeClass());
    ArrayType stringArray = ArrayType.ofComponentType(JavaTypes.STRING_TYPE);
    assertTrue(nullArray.isSubtypeOfOrEqualTo(stringArray));
    assertFalse(stringArray.isSubtypeOfOrEqualTo(nullArray));
  }

  /** A generic class with a non-generic member class. */
  static class Outer<T> {
    /** A non-generic member class of a generic class. */
    class Inner {}
  }

  /** A generic subclass of a member class of a parameterized type. */
  static class Sub<U> extends Outer<String>.Inner {
    /**
     * Creates a Sub.
     *
     * @param outer the enclosing instance of the superclass
     */
    Sub(Outer<String> outer) {
      outer.super();
    }
  }

  /** Methods whose return types are used in the tests. */
  @SuppressWarnings("unused")
  abstract static class TypeHolder {
    abstract Outer<String>.Inner innerType();

    abstract Outer<String>.Inner[] innerArrayType();

    abstract Sub<Integer> subType();

    abstract Sub<Integer>[] subArrayType();

    abstract List<? extends Number> extendsNumberList();

    abstract List<? extends Number>[] extendsNumberListArray();

    abstract List<? super Integer> superIntegerList();

    abstract List<Integer> integerList();

    abstract List<Integer>[] integerListArray();

    abstract List<String> stringList();

    abstract List<String>[] stringListArray();

    abstract List<?> unboundedList();

    abstract Collection<? super String> superStringCollection();

    abstract <T> List<? extends T[]> extendsVariableArrayList();

    abstract Collection<? extends Object[]> extendsObjectArrayCollection();

    abstract List<? super String> superStringList();

    abstract Collection<?> unboundedCollection();
  }

  /**
   * Returns the Randoop type for the return type of the given method of {@link TypeHolder}.
   *
   * @param methodName a method name of {@link TypeHolder}
   * @return the return type of the method
   */
  private static Type returnType(String methodName) throws NoSuchMethodException {
    return Type.forType(TypeHolder.class.getDeclaredMethod(methodName).getGenericReturnType());
  }

  @Test
  public void testSubtypeOfMemberClassOfParameterizedType() throws NoSuchMethodException {
    Type inner = returnType("innerType");
    Type sub = returnType("subType");
    assertTrue(sub.isSubtypeOfOrEqualTo(inner));
    assertFalse(inner.isSubtypeOfOrEqualTo(sub));
    assertFalse(returnType("integerList").isSubtypeOfOrEqualTo(inner));

    Type innerArray = returnType("innerArrayType");
    Type subArray = returnType("subArrayType");
    assertTrue(subArray.isSubtypeOfOrEqualTo(innerArray));
    assertFalse(returnType("integerListArray").isSubtypeOfOrEqualTo(innerArray));
  }

  @Test
  public void testSubtypeOfWildcardParameterizedType() throws NoSuchMethodException {
    Type extendsNumberList = returnType("extendsNumberList");
    Type superIntegerList = returnType("superIntegerList");
    Type integerList = returnType("integerList");
    Type stringList = returnType("stringList");
    assertTrue(integerList.isSubtypeOfOrEqualTo(extendsNumberList));
    assertFalse(stringList.isSubtypeOfOrEqualTo(extendsNumberList));
    assertTrue(integerList.isSubtypeOfOrEqualTo(superIntegerList));
    assertFalse(stringList.isSubtypeOfOrEqualTo(superIntegerList));

    Type extendsNumberListArray = returnType("extendsNumberListArray");
    assertTrue(returnType("integerListArray").isSubtypeOfOrEqualTo(extendsNumberListArray));
    assertFalse(returnType("stringListArray").isSubtypeOfOrEqualTo(extendsNumberListArray));
  }

  @Test
  public void testSubtypeOfLowerBoundedWildcardInheritedSupertype() throws NoSuchMethodException {
    Type superStringCollection = returnType("superStringCollection");
    assertTrue(returnType("stringList").isSubtypeOfOrEqualTo(superStringCollection));
    // JLS 4.5.1: `? super String` does not contain `?`.
    assertFalse(returnType("unboundedList").isSubtypeOfOrEqualTo(superStringCollection));
    Type superStringList = returnType("superStringList");
    assertTrue(superStringList.isSubtypeOfOrEqualTo(superStringCollection));
    // JLS 4.5.1: `?` contains `? super String`.
    assertTrue(superStringList.isSubtypeOfOrEqualTo(returnType("unboundedCollection")));
  }

  @Test
  public void testSubtypeOfWildcardWithVariableArrayBound() throws NoSuchMethodException {
    Type extendsObjectArrayCollection = returnType("extendsObjectArrayCollection");
    assertTrue(
        returnType("extendsVariableArrayList").isSubtypeOfOrEqualTo(extendsObjectArrayCollection));
    assertFalse(returnType("unboundedList").isSubtypeOfOrEqualTo(extendsObjectArrayCollection));
  }
}
