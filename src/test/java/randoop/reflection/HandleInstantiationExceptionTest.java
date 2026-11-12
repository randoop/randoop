package randoop.reflection;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import randoop.main.GenInputsAbstract;
import randoop.operation.TypedOperation;

/** Tests {@link TypeInstantiator#handleInstantiationException}. */
public class HandleInstantiationExceptionTest {

  /**
   * Tests that the exception is reported, not thrown, without {@code --fail-on-generation-error}.
   */
  @Test
  public void testWithoutFailOnGenerationError() throws Exception {
    TypedOperation operation = TypedOperation.forConstructor(Object.class.getConstructor());
    boolean saved = GenInputsAbstract.fail_on_generation_error;
    GenInputsAbstract.fail_on_generation_error = false;
    try {
      TypeInstantiator.handleInstantiationException(operation, new IllegalStateException());
    } finally {
      GenInputsAbstract.fail_on_generation_error = saved;
    }
  }

  /** Tests that the exception is thrown with {@code --fail-on-generation-error}. */
  @Test
  public void testWithFailOnGenerationError() throws Exception {
    TypedOperation operation = TypedOperation.forConstructor(Object.class.getConstructor());
    IllegalStateException cause = new IllegalStateException();
    boolean saved = GenInputsAbstract.fail_on_generation_error;
    GenInputsAbstract.fail_on_generation_error = true;
    try {
      RandoopInstantiationError e =
          assertThrows(
              RandoopInstantiationError.class,
              () -> TypeInstantiator.handleInstantiationException(operation, cause));
      assertSame(cause, e.getException());
      assertTrue(e.getOpName(), e.getOpName().contains("java.lang.Object()"));
    } finally {
      GenInputsAbstract.fail_on_generation_error = saved;
    }
  }
}
