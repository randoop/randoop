package randoop.experiments;

/**
 * Provides a list of Make targets for parallel execution.
 * 
 * Implementing classes must declare a constructor that takes
 * a String array as its sole input parameter.
 *
 */
public interface TargetMaker {

  /**
   * @return The next target, or null if no more targets.
   */
  String getNextTarget();
  
  /**
   *
   * @return true if there are more targets left.
   */
  boolean hasMoreTargets();

  int targetsLeft();

}
