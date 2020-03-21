package randoop.operation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;
import org.junit.Test;
import randoop.types.TypeVariable;

public class TypedOperationTest {
  @Test
  public void testOperationParameterTypes() {
    Class<?> c = ParameterInput.class;
    Method m;
    try {
      m = c.getMethod("m", Iterable.class, Iterable.class);
    } catch (NoSuchMethodException e) {
      fail("failed to load method ParameterInput.m()");
      throw new Error("unreachable");
    }
    TypedClassOperation operation = TypedOperation.forMethod(m);
    assertFalse(
        "operation has type parameters: " + operation, operation.getTypeParameters().isEmpty());
    TypedClassOperation capOp = operation.applyCaptureConversion();
    assertFalse(
        "cap converted op has type parameters: " + capOp, capOp.getTypeParameters().isEmpty());
    for (TypeVariable variable : operation.getTypeParameters()) {
      assertTrue(capOp.getTypeParameters().contains(variable));
    }
  }
}
