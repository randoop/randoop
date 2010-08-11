package randoop;

/**
 * Defines various event during Randoop's generation at which an
 * implementing class can be notified and perform some action. 
 */
public interface IEventListener {

  /**
   * Called by the AbstractGenerator immediately before a generation <code>step()</code> is performed.
   * 
   *  @see randoop.AbstractGenerator
   */
  void generationStepPre();
  
  /**
   * Called by the AbstractGenerator immediately after a generation <code>step()</code> has completed.
   *  
   * @param es
   * 
   *  @see randoop.AbstractGenerator
   */
  void generationStepPost(ExecutableSequence es);
  
  void progressThreadUpdate();
  
  boolean stopGeneration();

  void explorationStart();

  void explorationEnd();
  
}
