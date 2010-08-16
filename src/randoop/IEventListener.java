package randoop;

/**
 * Defines various event during Randoop's generation at which an
 * implementing class can be notified and perform some action.
 */
public interface IEventListener {

  /**
   * Called immediately at the start of test generation, before any generation steps have occurred.
   */
  void explorationStart();
  
  /**
   * Called immediately after the end of test generation. 
   */
  void explorationEnd();
  
  /**
   * Called by the AbstractGenerator during each generation iteration,
   * immediately before a generation <code>step()</code> is performed.
   * 
   *  @see randoop.AbstractGenerator
   */
  void generationStepPre();
  
  /**
   * Called by the AbstractGenerator during each generation iteration,
   * immediately after a generation <code>step()</code> has completed.
   *  
   * @param s sequence that was generated and executed in the last generation step.
   *        Can b null, which means the last step was unable to generate a sequence
   *        (e.g. due to a bad random choice). 
   * 
   *  @see randoop.AbstractGenerator
   */
  void generationStepPost(ExecutableSequence s);
  
  /**
   * Called by ProgressDisplay at regular intervals to monitor progress.
   * Implementing classes can use this opportunity to update state.
   * 
   * @see randoop.util.ProgressDisplay
   */
  void progressThreadUpdate();
  
  /**
   * Called by AbstractGenerator to determine if generation should stop.
   * True signals to the generator that generation should stop.
   * 
   * @see randoop.AbstractGenerator
   */
  boolean stopGeneration();

  
}
