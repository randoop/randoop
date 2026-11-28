package randoop.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static randoop.reflection.AccessibilityPredicate.IS_PUBLIC;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.StringJoiner;
import org.junit.Test;
import randoop.DummyVisitor;
import randoop.condition.RandoopSpecificationError;
import randoop.operation.TypedOperation;
import randoop.reflection.OmitMethodsPredicate;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Sequence;
import randoop.util.MultiMap;

/** Tests for {@link RegressionCaptureGenerator}. */
public class RegressionCaptureGeneratorTest {

  /** Methods whose results are arrays. */
  public static class ArrayProducers {

    /**
     * Returns an array, as its declared type.
     *
     * @return an array
     */
    public static int[] asArray() {
      return new int[] {1, 2};
    }

    /**
     * Returns an array, as a non-array type.
     *
     * @return an array
     */
    public static Serializable asSerializable() {
      return new int[] {1, 2};
    }
  }

  /**
   * Returns the code of the regression assertions for a sequence that calls the given method of
   * {@link ArrayProducers}.
   *
   * @param methodName the name of a method of {@link ArrayProducers}
   * @return the code of the assertions for a call to the method
   */
  private static String assertionCode(String methodName)
      throws NoSuchMethodException, RandoopSpecificationError {
    TypedOperation op = TypedOperation.forMethod(ArrayProducers.class.getMethod(methodName));
    Sequence sequence = new Sequence().extend(op, new ArrayList<>());
    ExecutableSequence eseq = new ExecutableSequence(sequence);
    eseq.execute(new DummyVisitor(), new DummyCheckGenerator());
    RegressionCaptureGenerator gen =
        new RegressionCaptureGenerator(
            new ExpectedExceptionCheckGen(IS_PUBLIC),
            new MultiMap<>(),
            IS_PUBLIC,
            OmitMethodsPredicate.NO_OMISSION,
            true);
    StringJoiner code = new StringJoiner(System.lineSeparator());
    for (Check check : gen.generateTestChecks(eseq).checks()) {
      code.add(check.toCodeStringPostStatement());
    }
    return code.toString();
  }

  /**
   * Tests that an array-equality assertion is generated only for a variable whose declared type is
   * an array type. For another variable, such as one of type {@code Serializable}, the assertion
   * would not compile.
   */
  @Test
  public void testArrayAssertionRequiresArrayType()
      throws NoSuchMethodException, RandoopSpecificationError {
    assertTrue(assertionCode("asArray").contains("assertArrayEquals"));
    assertFalse(assertionCode("asSerializable").contains("assertArrayEquals"));
  }
}
