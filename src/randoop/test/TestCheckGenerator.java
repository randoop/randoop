package randoop.test;

import randoop.sequence.ExecutableSequence;

/**
 * A check 
 */
public interface TestCheckGenerator {
  
  TestChecks visit(ExecutableSequence s);

}
