package randoop.types;

import static org.junit.Assert.fail;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.junit.Test;

import randoop.reflection.DefaultReflectionPredicate;

public class OperationsOfTypesTest {

  @Test
  public void test() {
    TypeFactory harvester = new TypeFactory(new DefaultReflectionPredicate());
    Class<?> c = GenericWithOperations.class;
    GeneralType t = harvester.forClass(c);

    GeneralType t2 = harvester.forClass(ConcreteWithOperations.class);

    for (Field f : c.getFields()) {
      Type fieldType = f.getGenericType();
      System.out.println(fieldType);
    }
    for (Constructor<?> con : c.getConstructors()) {
      Type[] argTypes = con.getGenericParameterTypes();
      for (Type argType : argTypes) {
        System.out.println(argType);
      }
    }
    for (Method m : c.getMethods()) {
      Type retType = m.getGenericReturnType();
      System.out.println(retType);
      Type[] argTypes = m.getGenericParameterTypes();
      for (Type argType : argTypes) {
        System.out.println(argType);
      }
    }

    fail("Not yet implemented");
  }

}
