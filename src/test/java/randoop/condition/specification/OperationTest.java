package randoop.condition.specification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import org.junit.Test;

public class OperationTest {

  @Test
  public void testConstructorPredicate() throws NoSuchMethodException {
    Class<?> c = randoop.condition.ClassWithConditions.class;
    Constructor<?> constructor;
    try {
      constructor = c.getConstructor(int.class);
    } catch (NoSuchMethodException e) {
      fail("Could not load constructor");
      throw new Error("dead code");
    }
    OperationSignature operation = OperationSignature.of(constructor);
    assertTrue(operation.isConstructor());

    assertEquals("randoop.condition.ClassWithConditions", operation.getName());
    assertEquals("randoop.condition.ClassWithConditions", operation.getClassname());

    Method method = c.getMethod("category", int.class);
    OperationSignature methodOperation = OperationSignature.of(method);
    assertFalse(methodOperation.isConstructor());
  }
}
