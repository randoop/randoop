package randoop.sequence;

import static org.junit.Assert.assertThrows;

import java.util.Collections;
import org.junit.Test;
import org.plumelib.util.SIList;
import randoop.condition.RandoopSpecificationError;
import randoop.main.GenInputsAbstract;
import randoop.operation.TypedOperation;
import randoop.types.JavaTypes;

/** Tests of the representation-invariant checks of {@link Sequence}. */
public class SequenceCheckRepTest {

  /**
   * Returns a sequence whose second statement passes a {@code String} to {@code
   * Integer.valueOf(int)}. Its representation invariant does not hold, so creating it while {@link
   * GenInputsAbstract#debug_checks} is true throws an exception. Its last statement is well-typed,
   * because the constructor of {@link Sequence} asserts that the last statement is well-typed even
   * when debug_checks is false.
   *
   * @return an ill-typed sequence
   * @throws NoSuchMethodException if {@code Integer.valueOf(int)} does not exist
   * @throws RandoopSpecificationError if a specification derived from the annotations of {@code
   *     Integer.valueOf(int)} does not compile
   */
  private static Sequence illTypedSequence()
      throws NoSuchMethodException, RandoopSpecificationError {
    Statement stringStatement =
        new Statement(TypedOperation.createPrimitiveInitialization(JavaTypes.STRING_TYPE, "hi"));
    Statement valueOfStatement =
        new Statement(
            TypedOperation.forMethod(Integer.class.getMethod("valueOf", int.class)),
            Collections.singletonList(new Sequence.RelativeNegativeIndex(-1)));
    Statement intStatement =
        new Statement(TypedOperation.createPrimitiveInitialization(JavaTypes.INT_TYPE, 1));
    return new Sequence(
        SIList.<Statement>empty().add(stringStatement).add(valueOfStatement).add(intStatement));
  }

  /**
   * Tests that a sequence created while {@link GenInputsAbstract#debug_checks} was false has all
   * its statements checked when it is extended or concatenated while debug_checks is true.
   */
  @Test
  public void testStatementsCreatedWithoutDebugChecksAreChecked()
      throws NoSuchMethodException, RandoopSpecificationError {
    boolean originalDebugChecks = GenInputsAbstract.debug_checks;
    try {
      GenInputsAbstract.debug_checks = true;
      assertThrows(IllegalArgumentException.class, () -> illTypedSequence());

      GenInputsAbstract.debug_checks = false;
      Sequence illTyped = illTypedSequence();

      GenInputsAbstract.debug_checks = true;
      TypedOperation intTerm = TypedOperation.createPrimitiveInitialization(JavaTypes.INT_TYPE, 1);
      assertThrows(
          IllegalArgumentException.class, () -> illTyped.extend(intTerm, Collections.emptyList()));
      Sequence intSequence = Sequence.createSequenceForPrimitive(1);
      assertThrows(
          IllegalArgumentException.class, () -> Sequence.concatenate(intSequence, illTyped));
    } finally {
      GenInputsAbstract.debug_checks = originalDebugChecks;
    }
  }
}
