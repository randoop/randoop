package randoop.test;

import randoop.sequence.ExecutableSequence;

/**
 * Generates a set of test checks from a test sequence that has been executed.
 *
 * <p>Note: a generator does not determine which sequences become output. Instead a generator
 * produces the {@link TestChecks} objects that, for valid behaviors, represent assertions in error
 * or regression tests.
 */
// Abstract class instead of an interface to permit default implementation of hasGenerator().
public abstract class TestCheckGenerator {

  /**
   * Generate a {@link TestChecks} object for the executed sequence {@code eseq} based on the
   * criteria of this generator.
   *
   * @param eseq the sequence for which checks are generated
   * @return the generated check set for the sequence
   */
  public abstract TestChecks<?> generateTestChecks(ExecutableSequence eseq);

  /**
   * Returns true if this generator contains a generator of the given class.
   *
   * @param genClass the generator class to search for in this
   * @return true iff this generator contains a generator of the given class
   */
  public boolean hasGenerator(Class<? extends TestCheckGenerator> genClass) {
    return this.getClass() == genClass;
  }
}
