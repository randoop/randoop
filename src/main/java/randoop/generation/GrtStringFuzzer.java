package randoop.generation;

import randoop.main.RandoopBug;
import randoop.sequence.Sequence;
import randoop.types.Type;
import randoop.util.Randomness;

/**
 * Fuzzer that builds a mutated {@link String} literal by applying a {@link StringFuzzingOperation}.
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

  /* ------------------------------- API ----------------------------------- */

  @Override
  public boolean canFuzz(Type type) {
    return type.getRuntimeClass() == String.class;
  }

  @Override
  public Sequence fuzz(Sequence sequence) {
    if (sequence.size() == 0) {
      throw new IllegalArgumentException("Cannot fuzz an empty Sequence");
    }

    Object lastValue = sequence.getStatement(sequence.size() - 1).getValue();
    final String mutated = mutate((String) lastValue);
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
        int pos = Randomness.nextRandomInt(len + 1);
        char c = randomPrintableChar();
        return s.substring(0, pos) + c + s.substring(pos);
      case REMOVE:
        if (len == 0) return s;
        int rpos = Randomness.nextRandomInt(len);
        return s.substring(0, rpos) + s.substring(rpos + 1);
      case REPLACE:
        if (len == 0) return s;
        int xpos = Randomness.nextRandomInt(len);
        char xc = randomPrintableChar();
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

  /**
   * Returns a random printable ASCII character.
   *
   * @return a random printable ASCII character
   */
  private static char randomPrintableChar() {
    // 95 is the span of ASCII characters:  32-126 inclusive.
    return (char) (32 + Randomness.nextRandomInt(95));
  }

  /* ------------------------- String fuzz enum ------------------------ */

  /**
   * Represents the fuzzing operations for Strings. Each run of GRT Fuzzing randomly selects one
   * operations to perform on the input String.
   */
  // This is public so that its documentation can be used rather than repeated elsewhere.
  public static enum StringFuzzingOperation {
    /** Insert a random character at a random index. */
    INSERT,
    /** Remove a character at a random index. */
    REMOVE,
    /** Replace a character at a random index with a random character. */
    REPLACE,
    /** Extract a substring from a random start index to a random end index. */
    SUBSTRING;

    /**
     * Returns a random StringFuzzingOperation.
     *
     * @return a random StringFuzzingOperation
     */
    static StringFuzzingOperation random() {
      return values()[Randomness.nextRandomInt(values().length)];
    }
  }
}
