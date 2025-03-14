package randoop.generation;

import randoop.sequence.ExecutableSequence;

/**
 * Defines various event during Randoop's generation at which an implementing class can be notified
 * and perform some action.
 */
public interface IEventListener {

  /**
   * Called immediately at the start of test generation, before any generation steps have occurred.
   */
  void explorationStart();

  /** Called immediately after the end of test generation. */
  void explorationEnd();

  /**
   * Called by the AbstractGenerator during each generation iteration, immediately before a
   * generation {@code step()} is performed.
   *
   * @see randoop.generation.AbstractGenerator
   */
  void generationStepPre();

  /**
   * Called by the AbstractGenerator during each generation iteration, immediately after a
   * generation {@code step()} has completed.
   *
   * @param eseq sequence that was generated and executed in the last generation step. Can b null,
   *     which means the last step was unable to generate a sequence (e.g. due to a bad random
   *     choice).
   * @see randoop.generation.AbstractGenerator
   */
  void generationStepPost(ExecutableSequence eseq);

  /**
   * Called by ProgressDisplay at regular intervals to monitor progress. Implementing classes can
   * use this opportunity to update state.
   *
   * @see randoop.util.ProgressDisplay
   */
  void progressThreadUpdate();

  /**
   * Called by AbstractGenerator to determine if generation should stop. True signals to the
   * generator that generation should stop.
   *
   * @return true if generation should stop, false otherwise
   * @see randoop.generation.AbstractGenerator
   */
  boolean shouldStopGeneration();
}
