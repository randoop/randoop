package randoop.condition;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;
import org.junit.Test;
import randoop.operation.TypedClassOperation;
import randoop.operation.TypedOperation;

/**
 * Tests that {@link TypedOperation#forMethod} derives a specification from a method's annotations,
 * and that a failure to do so is reported to the caller rather than being ignored.
 */
public class AnnotationSpecificationTest {

  /** A method whose annotation-derived specification compiles yields that specification. */
  @Test
  public void goodAnnotationTest() throws NoSuchMethodException, RandoopSpecificationError {
    Method method =
        AnnotatedParameterInput.class.getDeclaredMethod("accessibleParameterType", String.class);
    TypedClassOperation operation = TypedOperation.forMethod(method);
    ExecutableSpecification execSpec = operation.getExecutableSpecification();
    assertNotNull(execSpec);
    assertTrue("expected a derived precondition, got " + execSpec, !execSpec.isEmpty());
  }

  /**
   * A method whose annotation-derived specification does not compile makes {@link
   * TypedOperation#forMethod} throw, rather than silently returning an operation that lacks the
   * derived specification.
   */
  @Test
  public void badAnnotationTest() throws NoSuchMethodException {
    Method method = null;
    for (Method m : AnnotatedParameterInput.class.getDeclaredMethods()) {
      if (m.getName().equals("inaccessibleParameterType")) {
        method = m;
      }
    }
    assertNotNull(method);
    try {
      TypedClassOperation operation = TypedOperation.forMethod(method);
      fail("expected RandoopSpecificationError, got operation " + operation);
    } catch (RandoopSpecificationError e) {
      // This is the expected outcome.
      assertTrue(
          "unexpected message: " + e.getMessage(),
          e.getMessage().contains("Condition method did not compile"));
    }
  }
}
