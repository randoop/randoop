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
  public void testConstructorPredicate() {
    Class<?> c = randoop.condition.ClassWithConditions.class;
    Constructor<?> constructor = null;
    try {
      constructor = c.getConstructor(int.class);
    } catch (NoSuchMethodException e) {
      fail("Could not load constructor");
    }
    assert constructor != null;
    Operation operation = Operation.getOperation(constructor);
    assertTrue("operation is a constructor", operation.isConstructor());

    assertThat("name", operation.getName(), is(equalTo("ClassWithConditions")));
    assertThat(
        "classname",
        operation.getClassname(),
        is(equalTo("randoop.condition.ClassWithConditions")));

    Method method = null;
    try {
      method = c.getMethod("category", int.class);
    } catch (NoSuchMethodException e) {
      fail("Could not load method");
    }
    Operation methodOperation = Operation.getOperation(method);
    assertFalse("operation is not a constructor", methodOperation.isConstructor());
  }
}
