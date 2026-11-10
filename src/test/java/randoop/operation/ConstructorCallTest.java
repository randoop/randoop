package randoop.operation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Constructor;
import org.junit.Test;

/** Tests for {@link ConstructorCall}. */
public class ConstructorCallTest {

  /**
   * A constructor call is not a message: it has no receiver argument. {@code
   * randoop.generation.ForwardGenerator} uses {@code isMessage()} to decide whether the first input
   * to an operation is a receiver.
   */
  @Test
  public void testIsMessage() throws NoSuchMethodException {
    Constructor<String> constructor = String.class.getConstructor(String.class);
    ConstructorCall constructorCall = new ConstructorCall(constructor);
    assertFalse(constructorCall.isMessage());
    assertFalse(constructorCall.isMethodCall());
    assertTrue(constructorCall.isConstructorCall());
  }
}
