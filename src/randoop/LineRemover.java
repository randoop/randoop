package randoop;



// NOT WORKING. This used to be a utility class that tried to
// reduce the size of a failing tests to aid debugging.
public class LineRemover {

  //Statistics collected across all calls to minimize().
  protected static int original_sequence_size_accum = 0;
  protected static int minimized_sequence_size_accum = 0;
  protected static int times_minimize_was_called = 0;

  /**
   * Minimized the sequence by removing all statements that do not contribute to the given violations.
   * Returns the modified sequence or the original sequence if minimization fails.  
   */
  public static ExecutableSequence minimize(ExecutableSequence seq) {
    if (seq == null)
      throw new IllegalArgumentException("parameters cannot be null.");

    try {
      execute(seq);
    } catch (ReplayFailureException e) {
      return seq;
    }

    ExecutableSequence orig= seq.duplicate();

    ExecutableSequence replaced= replaceRemovedWithDummies(seq);
    if (replaced == null)
      return orig;
    return replaced;
  }

  //returns null if cannot minimize
  private static ExecutableSequence replaceRemovedWithDummies(ExecutableSequence seq) {
    times_minimize_was_called++;
    original_sequence_size_accum += seq.sequence.size();

    throw new RuntimeException("Not implemted.");
  }
  
  private static void execute(ExecutableSequence s) throws ReplayFailureException {
    throw new RuntimeException("not implemented.");
  }

  /**
   * The average size (number of method calls) of a sequence on which
   * minize() was called. Average is over all calls of minimize().
   */
  public static double averageOriginalSize() {
    return ((double)original_sequence_size_accum) / ((double)times_minimize_was_called);
  }

  /**
   * The average size (number of method calls) of a minimized sequence.
   * Average is over all calls of minimize().
   */

  public static double averageMinimizedSize() {
    return ((double)minimized_sequence_size_accum) / ((double)times_minimize_was_called);
  }

}
