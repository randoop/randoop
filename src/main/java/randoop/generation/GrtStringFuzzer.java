package randoop.generation;

import randoop.main.RandoopBug;
import randoop.sequence.Sequence;
import randoop.types.Type;
import randoop.util.Randomness;

/**
 * Fuzzer that builds a mutated {@link String} by <em>INSERT</em>, <em>REMOVE</em>,
 * <em>REPLACE</em>, or <em>SUBSTRING</em> operations at generation time, and emits a string
 * literal.
 *
 * <p>INSERT: Insert a random character at a random index. REMOVE: Remove a character at a random
 * index. REPLACE: Replace a character at a random index with a random character. SUBSTRING: Extract
 * a substring from a random start index to a random end index.
 */
public final class GrtStringFuzzer extends GrtFuzzer {

  /* --------------------------- Singleton --------------------------- */
  /** Singleton instance. */
  private static final GrtStringFuzzer INSTANCE = new GrtStringFuzzer();

  /**
   * Obtain the singleton instance of {@link GrtStringFuzzer}.
   *
   * @return the singleton instance
   */
  public static GrtStringFuzzer getInstance() {
    return INSTANCE;
  }

  /** Private constructor to enforce singleton. */
  private GrtStringFuzzer() {
    /* no-op */
  }

  /* ------------------------------ Constants ------------------------------ */
  /** The starting ASCII value for printable characters. */
  private static final int PRINTABLE_ASCII_START = 32;

  /** Number of printable ASCII characters (codes 32-126 inclusive). */
  private static final int PRINTABLE_ASCII_SPAN = 95;

  /* ------------------------------- API ----------------------------------- */
  @Override
  public boolean canFuzz(Type type) {
    return type.getRuntimeClass() == String.class;
  }

  @Override
  public Sequence fuzz(Sequence sequence) {
    if (sequence.size() == 0) {
      return sequence; // nothing to fuzz
    }

    // 1) Grab the last runtime value:
    Object lastValue = sequence.getStatement(sequence.size() - 1).getValue();

    // 2) If it's not a String, just skip fuzzing:
    if (!(lastValue instanceof String)) {
      return sequence;
    }

    final String strToFuzz = (String) lastValue;

    final String mutated = mutate(strToFuzz);

    // 4) Emit exactly one literal statement for the new String
    return Sequence.concatenate(sequence, Sequence.createSequenceForPrimitive(mutated));
  }

  /* ------------------------- Helper methods ------------------------------ */
  /**
   * Mutate a string by applying a random operation.
   *
   * @param s the string to mutate
   * @return the mutated string
   */
  private static String mutate(String s) {
    StringFuzzingOperation strFuzzingOp = StringFuzzingOperation.random();
    int len = s.length();
    switch (strFuzzingOp) {
      case INSERT:
        {
          int pos = Randomness.nextRandomInt(len + 1);
          char c = (char) (PRINTABLE_ASCII_START + Randomness.nextRandomInt(PRINTABLE_ASCII_SPAN));
          return s.substring(0, pos) + c + s.substring(pos);
        }
      case REMOVE:
        if (len == 0) return s;
        int rpos = Randomness.nextRandomInt(len);
        return s.substring(0, rpos) + s.substring(rpos + 1);
      case REPLACE:
        if (len == 0) return s;
        int xpos = Randomness.nextRandomInt(len);
        char xc = (char) (PRINTABLE_ASCII_START + Randomness.nextRandomInt(PRINTABLE_ASCII_SPAN));
        return s.substring(0, xpos) + xc + s.substring(xpos + 1);
      case SUBSTRING:
        if (len <= 1) return s;
        int i1 = Randomness.nextRandomInt(len - 1);
        int i2 = Randomness.nextRandomInt(len - i1) + i1 + 1;
        return s.substring(i1, i2);
      default:
        throw new RandoopBug("Unknown string fuzz op: " + strFuzzingOp);
    }
  }

  /* ------------------------- String fuzz enum ------------------------ */
  /**
   * An enum representing the fuzzing operations for Strings. Each run of GRT Fuzzing will randomly
   * select one of these set of operations to perform on the input String.
   */
  private enum StringFuzzingOperation {
    INSERT,
    REMOVE,
    REPLACE,
    SUBSTRING;

    /** The set of all StringFuzzingOperation values. */
    private static final StringFuzzingOperation[] VALUES = values();

    /**
     * Return a random StringFuzzingOperation.
     *
     * @return a random StringFuzzingOperation
     */
    static StringFuzzingOperation random() {
      return VALUES[Randomness.nextRandomInt(VALUES.length)];
    }
  }
}
