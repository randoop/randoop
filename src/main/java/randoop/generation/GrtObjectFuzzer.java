package randoop.generation;

import java.util.ArrayList;
import java.util.List;
import randoop.operation.TypedOperation;
import randoop.sequence.Sequence;
import randoop.types.Type;

/**
 * Fuzzer that builds a mutated {@link Object} by applying a side-effecting operation on the last
 * value of a {@link Sequence}.
 */
public final class GrtObjectFuzzer extends GrtFuzzer {

  /* --------------------------- Singleton --------------------------- */

  /** Singleton instance. */
  private static final GrtObjectFuzzer INSTANCE = new GrtObjectFuzzer();

  /**
   * List of operations that can be applied to mutate the last value of a sequence. All operations
   * have some side effects, annotated with {@code @Impure}.
   */
  private final List<TypedOperation> operations = new ArrayList<>();

  /**
   * Obtain the singleton instance of {@link GrtStringFuzzer}.
   *
   * @return the singleton instance
   */
  public static GrtObjectFuzzer getInstance() {
    return INSTANCE;
  }

  /** Private constructor to enforce singleton. */
  private GrtObjectFuzzer() {
    /* no-op */
  }

  /* ------------------------------- API ----------------------------------- */

  @Override
  public boolean canFuzz(Type type) {
    return !type.isNonreceiverType();
  }

  /**
   * Add a list of side-effecting operations to the fuzzer.
   *
   * @param operations a list of operations to add, all containing side effects (annotated with
   *     Checker Framework's {@code @Impure})
   */
  public void addOperations(List<TypedOperation> operations) {
    if (operations == null) {
      throw new IllegalArgumentException("Operations list cannot be null");
    }
    this.operations.addAll(operations);
  }

  public Sequence fuzz(Sequence sequence) {
    if (sequence.size() == 0) {
      throw new IllegalArgumentException("Cannot fuzz an empty Sequence");
    }

    // TODO: Implement the fuzzing logic using the operations.
    return mutate(sequence);
  }

  /* ------------------------- Helper methods ------------------------------ */

  /**
   * Mutate the last value of the sequence by applying a side-effecting operation.
   *
   * @param sequence the sequence containing the last value to mutate
   * @return the mutated sequence
   */
  private static Sequence mutate(Sequence sequence) {
    return sequence;
  }
}
