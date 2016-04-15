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
    try {
      ConcreteType primitiveType = ConcreteType.forClass(int.class);
      assertEquals("builds primitive correctly", new ConcreteSimpleType(int.class), primitiveType);

      ConcreteType classType = ConcreteType.forClass(String.class);
      assertEquals("builds class type correctly", new ConcreteSimpleType(String.class), classType);

      Class<?> arrayClass = Array.newInstance(String.class, 0).getClass();
      ConcreteType arrayType = ConcreteType.forClass(arrayClass);
      assertEquals(
              "builds array type correctly",
              new ConcreteArrayType(new ConcreteSimpleType(String.class)),
              arrayType);

      ConcreteType rawClassType = ConcreteType.forClass(ArrayList.class);
      assertEquals(
              "builds raw class type correctly", new ConcreteSimpleType(ArrayList.class), rawClassType);

      try {
        ConcreteType badParameterizedType =
                ConcreteType.forClass(ArrayList.class, new ConcreteSimpleType(int.class));
        fail("illegal argument exception expected");
      } catch (IllegalArgumentException e) {
        assertEquals(
                "exception message", "type arguments may not be primitive (found: int)", e.getMessage());
      }

      ConcreteType boundedPType =
              ConcreteType.forClass(BoxClass.class, new ConcreteSimpleType(String.class));
      GenericType boundedGCType = GenericType.forClass(BoxClass.class);
      ConcreteType instantiatedType = boundedGCType.instantiate(new ConcreteSimpleType(String.class));
      assertEquals("ConcreteType.forClass vs instantiation", boundedPType, instantiatedType);
    } catch (RandoopTypeException e) {
      fail("type error: " + e);
    }
  }
}
