package randoop.test;

import randoop.sequence.ExecutableSequence;

/**
 * Generates a set of test checks from a test sequence that has been executed.
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
