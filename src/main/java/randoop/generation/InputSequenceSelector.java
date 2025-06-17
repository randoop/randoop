package randoop.generation;

import randoop.sequence.ExecutableSequence;
import randoop.sequence.Sequence;
import randoop.util.list.SimpleList;

/** Interface for selecting sequences as input for creating new sequences. */
public abstract class InputSequenceSelector {
  /**
   * Choose a sequence used as input for creating a new sequence.
   *
   * @param candidates sequences to choose from
   * @return the chosen sequence
   */
  public abstract Sequence selectInputSequence(SimpleList<Sequence> candidates);

  /**
   * A hook that is called after a new sequence has been created and executed.
   *
   * <p>The default implementation does nothing. Subclasses may override it to add behavior.
   *
   * @param eSeq the recently executed sequence which is new and unique, and has just been executed
   */
  public void createdExecutableSequence(ExecutableSequence eSeq) {}
}
