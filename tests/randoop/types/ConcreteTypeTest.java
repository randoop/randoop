package randoop.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.lang.reflect.Array;
import java.util.ArrayList;

import org.junit.Test;

import randoop.types.test.BoxClass;

public class ConcreteTypeTest {

  @Test
  public void testForClass() {
    ConcreteType primitiveType = ConcreteType.forClass(int.class);
    assertEquals("builds primitive correctly", new SimpleType(int.class), primitiveType);
    
    ConcreteType classType = ConcreteType.forClass(String.class);
    assertEquals("builds class type correctly", new SimpleType(String.class), classType);
    
    Class<?> arrayClass = Array.newInstance(String.class, 0).getClass();
    ConcreteType arrayType = ConcreteType.forClass(arrayClass);
    assertEquals("builds array type correctly", new ArrayType(new SimpleType(String.class)), arrayType);
    
    ConcreteType rawClassType = ConcreteType.forClass(ArrayList.class);
    assertEquals("builds raw class type correctly", new SimpleType(ArrayList.class), rawClassType);
        
    try {
      ConcreteType badParameterizedType = ConcreteType.forClass(ArrayList.class, new SimpleType(int.class));
      fail("illegal argument exception expected");
    } catch (IllegalArgumentException e) {
      assertEquals("exception message", "type arguments may not be primitive (found: int)", e.getMessage());
    }
    
    ConcreteType boundedPType = ConcreteType.forClass(BoxClass.class, new SimpleType(String.class));
    GenericType boundedGCType = GenericType.forClass(BoxClass.class);
    ConcreteType instantiatedType = boundedGCType.instantiate(new SimpleType(String.class));
    assertEquals("ConcreteType.forClass vs instantiation", boundedPType, instantiatedType);
  }

}
