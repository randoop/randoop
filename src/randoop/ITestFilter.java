package randoop;

/**
 * Used by Randoop's test generator to determine if a given sequence should be output to the user.
 * 
 * @see randoop.AbstractGenerator
 */
public interface ITestFilter {

  /**
   * Given sequence s and associated failure set f, returns true if
   * s should be output to the user as a test case.
   */
  boolean outputSequence(ExecutableSequence s, FailureSet f);

}
