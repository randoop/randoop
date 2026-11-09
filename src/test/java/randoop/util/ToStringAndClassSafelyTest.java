package randoop.util;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.AbstractList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import randoop.NormalExecution;
import randoop.contract.EqualsReflexive;
import randoop.contract.ObjectContract;

/**
 * Tests for {@link Util#toStringAndClassSafely}, and for the code that uses it to print values
 * whose {@code toString()} throws.
 */
public class ToStringAndClassSafelyTest {

  /** A class whose {@code toString()} throws an error. */
  public static class ToStringThrows {
    @Override
    public String toString() {
      throw new AssertionError();
    }
  }

  /** The result of printing a {@link ToStringThrows}. */
  private static final String TO_STRING_THROWS =
      "exception_when_calling_toString [class randoop.util.ToStringAndClassSafelyTest$ToStringThrows]:"
          + " java.lang.AssertionError";

  /** A list of size 1 whose {@code get()} throws, so iterating over it throws. */
  public static class GetThrowsList extends AbstractList<Object> {
    @Override
    public Object get(int index) {
      throw new ConcurrentModificationException();
    }

    @Override
    public int size() {
      return 1;
    }
  }

  /** An exception whose {@code getMessage()} throws an error. */
  public static class GetMessageThrowsException extends RuntimeException {
    /** ID for serializing this class. */
    private static final long serialVersionUID = 20261108L;

    @Override
    public String getMessage() {
      throw new AssertionError();
    }
  }

  /** A class whose constructor takes an argument; used to test constructor calls. */
  public static class Holder {
    /**
     * Creates a Holder.
     *
     * @param o a value, which is ignored
     */
    public Holder(Object o) {}

    @Override
    public String toString() {
      return "a Holder";
    }
  }

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

  /**
   * Returns its argument.
   *
   * @param o a value
   * @return {@code o}
   */
  public static Object identity(Object o) {
    return o;
  }

  /**
   * Throws a {@link GetMessageThrowsException}.
   *
   * @param o a value, which is ignored
   * @return never returns
   */
  public static Object throwGetMessageThrowsException(Object o) {
    throw new GetMessageThrowsException();
  }

  @Test
  public void testOrdinaryValues() {
    assertEquals("null", Util.toStringAndClassSafely(null));
    assertEquals("hello [class java.lang.String]", Util.toStringAndClassSafely("hello"));
    assertEquals("hello", Util.toStringSafely("hello"));
  }

  @Test
  public void testToStringThrows() {
    assertEquals(TO_STRING_THROWS, Util.toStringAndClassSafely(new ToStringThrows()));
    assertEquals(TO_STRING_THROWS, Util.toStringSafely(new ToStringThrows()));
    assertEquals(
        "exception_when_calling_toString [class java.util.HashMap]: java.lang.StackOverflowError",
        Util.toStringAndClassSafely(selfContainingMap(), true));
  }

  @Test
  public void testPrintingElementsThrows() {
    assertEquals(
        "exception_when_printing_elements [class"
            + " randoop.util.ToStringAndClassSafelyTest$GetThrowsList]:"
            + " java.util.ConcurrentModificationException",
        Util.toStringAndClassSafely(new GetThrowsList()));
  }

  @Test
  public void testElements() {
    assertEquals(
        "[{} [class java.util.HashMap], x [class java.lang.String], null, "
            + TO_STRING_THROWS
            + "]",
        Util.elementsToStringAndClassSafely(
            new Object[] {new HashMap<>(), "x", null, new ToStringThrows()}));
  }

  @Test
  public void testNormalExecution() {
    assertEquals(
        "[NormalExecution " + TO_STRING_THROWS + "]",
        new NormalExecution(new ToStringThrows(), 0).toString());
  }

  @Test
  public void testMethodReflectionCodeReturns() throws NoSuchMethodException {
    Method identity = ToStringAndClassSafelyTest.class.getDeclaredMethod("identity", Object.class);
    MethodReflectionCode code =
        new MethodReflectionCode(identity, null, new Object[] {new ToStringThrows()});
    code.runReflectionCode();
    assertEquals(
        "MethodReflectionCode: "
            + identity
            + " receiver: null args: ["
            + TO_STRING_THROWS
            + "] returned: "
            + TO_STRING_THROWS,
        code.toString());
  }

  @Test
  public void testMethodReflectionCodeThrows() throws NoSuchMethodException {
    Method thrower =
        ToStringAndClassSafelyTest.class.getDeclaredMethod(
            "throwGetMessageThrowsException", Object.class);
    MethodReflectionCode code = new MethodReflectionCode(thrower, null, new Object[] {"x"});
    code.runReflectionCode();
    assertEquals(
        "MethodReflectionCode: "
            + thrower
            + " receiver: null args: [x [class java.lang.String]] threw:"
            + " exception_when_calling_toString [class"
            + " randoop.util.ToStringAndClassSafelyTest$GetMessageThrowsException]:"
            + " java.lang.AssertionError",
        code.toString());
  }

  @Test
  public void testConstructorReflectionCode() throws NoSuchMethodException {
    Constructor<Holder> constructor = Holder.class.getConstructor(Object.class);
    ConstructorReflectionCode code =
        new ConstructorReflectionCode(constructor, new Object[] {new ToStringThrows()});
    code.runReflectionCode();
    assertEquals(
        "ConstructorReflectionCode: "
            + constructor
            + ", args: ["
            + TO_STRING_THROWS
            + "] returned: a Holder [class randoop.util.ToStringAndClassSafelyTest$Holder]",
        code.toString());
  }

  @Test
  public void testObjectContractReflectionCode() {
    ObjectContract contract = EqualsReflexive.getInstance();
    ObjectContractReflectionCode code =
        new ObjectContractReflectionCode(contract, new ToStringThrows());
    code.runReflectionCode();
    assertEquals(
        "Check of ObjectContract "
            + contract
            + " args: ["
            + TO_STRING_THROWS
            + "] returned: true [class java.lang.Boolean]",
        code.toString());
  }
}
