package randoop.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Test;

public class ParameterizedTypeTest {

  @Test
  public void testAssignability() {
    ConcreteType strALType = ConcreteType.forClass(ArrayList.class, new SimpleType(String.class));
    ConcreteType intALType = ConcreteType.forClass(ArrayList.class, new SimpleType(Integer.class));
    ConcreteType objALType = ConcreteType.forClass(ArrayList.class, new SimpleType(Object.class));
    ConcreteType rawALType = new SimpleType(ArrayList.class);
    
    assertTrue("ArrayList<String> can be assigned to itself", strALType.isAssignableFrom(strALType));
    assertFalse("ArrayList<Integer> cannot be assigned to ArrayList<String>", strALType.isAssignableFrom(intALType));
    assertTrue("ArrayList can be assigned to ArrayList<String>", strALType.isAssignableFrom(rawALType));
    assertFalse("ArrayList<Integer> cannot be assigned to ArrayList<Number>", objALType.isAssignableFrom(intALType));
    
    ConcreteType intType = new SimpleType(Integer.class);
    ConcreteType intCompType = ConcreteType.forClass(Comparable.class, new SimpleType(Integer.class));
    assertTrue("Integer assignable to Comparable<Integer>", intCompType.isAssignableFrom(intType));
    assertFalse("Comparable<Integer> not assignable to Integer", intType.isAssignableFrom(intCompType));
    
    ConcreteType strCompType = ConcreteType.forClass(Comparable.class, new SimpleType(String.class));
    assertTrue("Comparable<Integer> assignable to Comparable<Integer>", intCompType.isAssignableFrom(intCompType));
    assertFalse("Comparable<Integer> is not assignable from Comparable<String>", intCompType.isAssignableFrom(strCompType));
    
    ConcreteType intArrayType = new ArrayType(new SimpleType(Integer.class));
    assertFalse("Comparable<Integer> not assignable from Integer[]", intCompType.isAssignableFrom(intArrayType));
    
    // class A<T> implements Comparable<T> {}
    ConcreteType intAType = ConcreteType.forClass(A.class, new SimpleType(Integer.class));
    assertTrue("A<Integer> assignable to Comparable<Integer>", intCompType.isAssignableFrom(intAType));
    
    // class B extends A<String> {}
    ConcreteType strAType = ConcreteType.forClass(A.class, new SimpleType(String.class));
    ConcreteType bType = new SimpleType(B.class);
    assertTrue("B assignable to A<String>", strAType.isAssignableFrom(bType));
    assertTrue("B assignable to Comparable<String>", strCompType.isAssignableFrom(bType));
    
    // class C extends A<Integer> {}
    ConcreteType cType = new SimpleType(C.class);
    assertFalse("C not assignable to A<String>", strAType.isAssignableFrom(cType));
    assertFalse("C not assignable to Comparable<String>", strCompType.isAssignableFrom(cType));
 
    // class H<T> extends G<T> implements Comparable<T> {}
    ConcreteType strHType = ConcreteType.forClass(H.class, new SimpleType(String.class));
    ConcreteType strGType = ConcreteType.forClass(G.class, new SimpleType(String.class));
    assertTrue("H<String> assignable to G<String>", strGType.isAssignableFrom(strHType));
    assertTrue("H<String> assignable to Comparable<String>", strCompType.isAssignableFrom(strHType));
    
    // class D<S,T> extends A<T> {}
    ConcreteType strIntDType = ConcreteType.forClass(D.class, new SimpleType(String.class), new SimpleType(Integer.class));
    assertTrue("D<String,Integer> assignable to A<Integer>", intAType.isAssignableFrom(strIntDType));
    assertFalse("D<String,Integer> not assignable to A<String>", strAType.isAssignableFrom(strIntDType));
    
    // class E<S,T> {}
    ConcreteType strIntEType = ConcreteType.forClass(E.class, new SimpleType(String.class), new SimpleType(Integer.class));
    // class F<T,S> extends E<S,T> {}
    ConcreteType intStrFType = ConcreteType.forClass(F.class, new SimpleType(Integer.class), new SimpleType(String.class));
    assertTrue("F<Integer,String> assignable to E<String,Integer>", strIntEType.isAssignableFrom(intStrFType));
    ConcreteType strIntFType = ConcreteType.forClass(F.class, new SimpleType(String.class), new SimpleType(Integer.class));
    assertFalse("F<String,Integer> not assignable to E<String,Integer>", strIntEType.isAssignableFrom(strIntFType));
  }
  
  @Test
  public void testNames() {
    ConcreteType strALType = ConcreteType.forClass(ArrayList.class, new SimpleType(String.class));
    assertEquals("parameterized type name ", "java.util.ArrayList<java.lang.String>", strALType.getName());
  }
}
