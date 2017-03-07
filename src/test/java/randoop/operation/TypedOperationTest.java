package randoop.operation;

import org.junit.Test;

import java.lang.reflect.Method;

import randoop.types.TypeVariable;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TypedOperationTest {
  @Test
  public void testOperationParameterTypes() {
    Class<?> c = ParameterInput.class;
    Method m = null;
    try {
      m = c.getMethod("m", Iterable.class, Iterable.class);
    } catch (NoSuchMethodException e) {
      fail("failed to load method ParameterInput.m()");
    }
    assert m != null;
    TypedClassOperation operation = TypedOperation.forMethod(m);
    assertFalse(
        "operation has type parameters: " + operation, operation.getTypeParameters().isEmpty());
    TypedClassOperation capOp = operation.applyCaptureConversion();
    assertFalse(
        "cap converted op has type parameters: " + capOp, capOp.getTypeParameters().isEmpty());
    for (TypeVariable variable : operation.getTypeParameters()) {
      assertTrue(
          "wildcard params in cap converted params", capOp.getTypeParameters().contains(variable));
    }
  }
}
