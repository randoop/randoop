package randoop.test;

import randoop.sequence.ExecutableSequence;

/**
 * Generates a set of test checks from a test sequence that has been executed.
 * 
 * Note: a generator does not determine which sequences become output. 
 * Instead a generator produces the {@link TestChecks} objects that, for valid
 * behaviors, represent assertions in error or regression tests. 
 */
public interface TestCheckGenerator {
  
  /**
   * Generate a {@code TestChecks} object for the executed sequence {@code s} 
   * based on the criteria of this generator.
   * 
   * @param s  the sequence for which checks are generated
   * @return the generated check set for the sequence
   */
  TestChecks visit(ExecutableSequence s);

}
