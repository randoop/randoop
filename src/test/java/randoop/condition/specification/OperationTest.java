package randoop.condition.specification;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
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
    assertTrue("operation is a constructor", operation.isConstructor());

    assertThat("name", operation.getName(), is(equalTo("randoop.condition.ClassWithConditions")));
    assertThat(
        "classname",
        operation.getClassname(),
        is(equalTo("randoop.condition.ClassWithConditions")));

    Method method = c.getMethod("category", int.class);
    OperationSignature methodOperation = OperationSignature.of(method);
    assertFalse("operation is not a constructor", methodOperation.isConstructor());
  }
}
