package randoop.types;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

public class ArrayTypeTest {

  @Test
  public void testAssignability() {
    ConcreteType intArrType = new ArrayType(new SimpleType(int.class));
    ConcreteType shortArrType = new ArrayType(new SimpleType(short.class));
    ConcreteType strALArrType = new ArrayType(ConcreteType.forClass(ArrayList.class, new SimpleType(String.class)));
    ConcreteType intALArrType = new ArrayType(ConcreteType.forClass(ArrayList.class, new SimpleType(Integer.class)));
    ConcreteType alArrType = new ArrayType(new SimpleType(ArrayList.class));
    ConcreteType objArrType = new ArrayType(new SimpleType(Object.class));
    ConcreteType intBoxArrType = new ArrayType(new SimpleType(Integer.class));
    
    assertTrue("can assign array of same element type", intArrType.isAssignableFrom(intArrType));
    assertTrue("can assign array of raw type to array of parameterized type", strALArrType.isAssignableFrom(alArrType));
    assertTrue("can assign Integer[] to Object[]", objArrType.isAssignableFrom(intBoxArrType));
    assertTrue("can assign ArrayList<Integer>[] to Object[]", objArrType.isAssignableFrom(intALArrType));
    
    assertFalse("cannot assign short array to int array", intArrType.isAssignableFrom(shortArrType));
    assertFalse("cannot assign ArrayList<String> array to ArrayList<Integer> array", intALArrType.isAssignableFrom(strALArrType));
    assertFalse("cannot assign int array to Object array", objArrType.isAssignableFrom(intArrType));
  }

  @Test
  public void testNames() {
    ConcreteType intArrType = new ArrayType(new SimpleType(int.class));
    ConcreteType strArrType = new ArrayType(new SimpleType(String.class));
    ConcreteType intALArrType = new ArrayType(ConcreteType.forClass(ArrayList.class, new SimpleType(Integer.class)));
    ConcreteType alArrType = new ArrayType(new SimpleType(ArrayList.class));
    
    assertEquals("type name", "int[]", intArrType.getName());
    assertEquals("type name", "java.lang.String[]", strArrType.getName());
    assertEquals("type name", "java.util.ArrayList<java.lang.Integer>[]", intALArrType.getName());
    assertEquals("type name", "java.util.ArrayList[]", alArrType.getName());
  }
}
