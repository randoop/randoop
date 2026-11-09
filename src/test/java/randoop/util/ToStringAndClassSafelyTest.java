package randoop.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import randoop.NormalExecution;

/**
 * Tests for {@link Util#toStringAndClassSafely}, and for the code that uses it to print values
 * whose {@code toString()} throws.
 */
public class ToStringAndClassSafelyTest {

  /**
   * Returns a map that contains an unmodifiable view of itself. Its {@code toString()} throws
   * {@code StackOverflowError}, because {@code HashMap.toString()} detects only a map that contains
   * itself directly.
   *
   * @return a map whose {@code toString()} throws {@code StackOverflowError}
   */
  private static Map<Object, Object> selfContainingMap() {
    Map<Object, Object> map = new HashMap<>();
    map.put("key", Collections.unmodifiableMap(map));
    return map;
  }

  @Test
  public void testOrdinaryValues() {
    assertEquals("null", Util.toStringAndClassSafely(null));
    assertEquals("hello [class java.lang.String]", Util.toStringAndClassSafely("hello"));
  }

  @Test
  public void testToStringThrows() {
    assertEquals(
        "[java.util.HashMap whose toString() threw java.lang.StackOverflowError]",
        Util.toStringAndClassSafely(selfContainingMap()));
  }

  @Test
  public void testNormalExecution() {
    assertEquals(
        "[NormalExecution [java.util.HashMap whose toString() threw"
            + " java.lang.StackOverflowError]]",
        new NormalExecution(selfContainingMap(), 0).toString());
  }

  @Test
  public void testMethodReflectionCode() throws NoSuchMethodException {
    Map<Object, Object> map = selfContainingMap();
    Method identity = ToStringAndClassSafelyTest.class.getDeclaredMethod("identity", Object.class);
    MethodReflectionCode code = new MethodReflectionCode(identity, null, new Object[] {map});
    code.runReflectionCode();
    assertNull(code.getExceptionThrown());
    assertSame(map, code.getReturnValue());
    String threw = "[java.util.HashMap whose toString() threw java.lang.StackOverflowError]";
    assertTrue(code.toString().contains(" args: [" + threw + "]"));
    assertTrue(code.toString().endsWith(" returned: " + threw));
  }

  /**
   * Returns its argument.
   *
   * @param o a value
   * @return {@code o}
   */
  public static Object identity(Object o) {
    return o;
  }
}
