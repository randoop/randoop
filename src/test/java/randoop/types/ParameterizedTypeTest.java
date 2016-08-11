package randoop.types;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ParameterizedTypeTest {

  @Test
  public void testAssignability() {
    Type strALType =
        GenericClassType.forClass(ArrayList.class)
            .instantiate(new SimpleClassOrInterfaceType(String.class));
    Type intALType =
        GenericClassType.forClass(ArrayList.class)
            .instantiate(new SimpleClassOrInterfaceType(Integer.class));
    Type objALType =
        GenericClassType.forClass(ArrayList.class)
            .instantiate(new SimpleClassOrInterfaceType(Object.class));
    Type rawALType = new SimpleClassOrInterfaceType(ArrayList.class);

    assertTrue(
        "ArrayList<String> can be assigned to itself", strALType.isAssignableFrom(strALType));
    assertFalse(
        "ArrayList<Integer> cannot be assigned to ArrayList<String>",
        strALType.isAssignableFrom(intALType));
    assertTrue(
        "ArrayList can be assigned to ArrayList<String>", strALType.isAssignableFrom(rawALType));
    assertFalse(
        "ArrayList<Integer> cannot be assigned to ArrayList<Number>",
        objALType.isAssignableFrom(intALType));

    Type intType = new SimpleClassOrInterfaceType(Integer.class);
    Type intCompType =
        GenericClassType.forClass(Comparable.class)
            .instantiate(new SimpleClassOrInterfaceType(Integer.class));
    assertTrue("Integer assignable to Comparable<Integer>", intCompType.isAssignableFrom(intType));
    assertFalse(
        "Comparable<Integer> not assignable to Integer", intType.isAssignableFrom(intCompType));

    Type strCompType =
        GenericClassType.forClass(Comparable.class)
            .instantiate(new SimpleClassOrInterfaceType(String.class));
    assertTrue(
        "Comparable<Integer> assignable to Comparable<Integer>",
        intCompType.isAssignableFrom(intCompType));
    assertFalse(
        "Comparable<Integer> is not assignable from Comparable<String>",
        intCompType.isAssignableFrom(strCompType));

    Type intArrayType = ArrayType.ofElementType(new SimpleClassOrInterfaceType(Integer.class));
    assertFalse(
        "Comparable<Integer> not assignable from Integer[]",
        intCompType.isAssignableFrom(intArrayType));

    // class A<T> implements Comparable<T> {}
    Type intAType =
        GenericClassType.forClass(A.class)
            .instantiate(new SimpleClassOrInterfaceType(Integer.class));
    assertTrue(
        "A<Integer> assignable to Comparable<Integer>", intCompType.isAssignableFrom(intAType));

    // class B extends A<String> {}
    Type strAType =
        GenericClassType.forClass(A.class)
            .instantiate(new SimpleClassOrInterfaceType(String.class));
    Type bType = new SimpleClassOrInterfaceType(B.class);
    assertTrue("B assignable to A<String>", strAType.isAssignableFrom(bType));
    assertTrue("B assignable to Comparable<String>", strCompType.isAssignableFrom(bType));

    // class C extends A<Integer> {}
    Type cType = new SimpleClassOrInterfaceType(C.class);
    assertFalse("C not assignable to A<String>", strAType.isAssignableFrom(cType));
    assertFalse("C not assignable to Comparable<String>", strCompType.isAssignableFrom(cType));

    // class H<T> extends G<T> implements Comparable<T> {}
    Type strHType =
        GenericClassType.forClass(H.class)
            .instantiate(new SimpleClassOrInterfaceType(String.class));
    Type strGType =
        GenericClassType.forClass(G.class)
            .instantiate(new SimpleClassOrInterfaceType(String.class));
    assertTrue("H<String> assignable to G<String>", strGType.isAssignableFrom(strHType));
    assertTrue(
        "H<String> assignable to Comparable<String>", strCompType.isAssignableFrom(strHType));

    // class D<S,T> extends A<T> {}
    Type strIntDType =
        GenericClassType.forClass(D.class)
            .instantiate(
                new SimpleClassOrInterfaceType(String.class),
                new SimpleClassOrInterfaceType(Integer.class));
    assertTrue(
        "D<String,Integer> assignable to A<Integer>", intAType.isAssignableFrom(strIntDType));
    assertFalse(
        "D<String,Integer> not assignable to A<String>", strAType.isAssignableFrom(strIntDType));

    // class E<S,T> {}
    Type strIntEType =
        GenericClassType.forClass(E.class)
            .instantiate(
                new SimpleClassOrInterfaceType(String.class),
                new SimpleClassOrInterfaceType(Integer.class));
    // class F<T,S> extends E<S,T> {}
    Type intStrFType =
        GenericClassType.forClass(F.class)
            .instantiate(
                new SimpleClassOrInterfaceType(Integer.class),
                new SimpleClassOrInterfaceType(String.class));
    assertTrue(
        "F<Integer,String> assignable to E<String,Integer>",
        strIntEType.isAssignableFrom(intStrFType));
    Type strIntFType =
        GenericClassType.forClass(F.class)
            .instantiate(
                new SimpleClassOrInterfaceType(String.class),
                new SimpleClassOrInterfaceType(Integer.class));
    assertFalse(
        "F<String,Integer> not assignable to E<String,Integer>",
        strIntEType.isAssignableFrom(strIntFType));
  }

  @Test
  public void testNames() {
    Type strALType =
        GenericClassType.forClass(ArrayList.class)
            .instantiate(new SimpleClassOrInterfaceType(String.class));
    assertEquals(
        "parameterized type name ", "java.util.ArrayList<java.lang.String>", strALType.getName());
  }
}
