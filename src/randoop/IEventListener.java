package randoop;

/**
 * Defines various event during Randoop's generation at which an
 * implementing class can be notified and perform some action. 
 */
public interface IEventListener {

  void generationStepPre();
  
  void generationStepPost(ExecutableSequence es);
  
  void progressThreadUpdate();
  
  boolean stopGeneration();

  void explorationStart();

  void explorationEnd();
  
}
