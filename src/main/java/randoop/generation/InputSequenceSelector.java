package randoop.generation;

import java.util.Set;
import org.plumelib.util.SIList;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Sequence;

/** Interface for selecting sequences as input for creating new sequences. */
public abstract class InputSequenceSelector {

  /** Creates an InputSequenceSelector. */
  public InputSequenceSelector() {}

  /**
   * Choose a sequence used as input for creating a new sequence.
   *
   * @param candidates sequences to choose from
   * @return the chosen sequence
   */
  public abstract Sequence selectInputSequence(SIList<Sequence> candidates);

  /**
   * A hook that is called after a new sequence has been created and executed.
   *
   * <p>The default implementation does nothing. Subclasses may override it to add behavior.
   *
   * @param eSeq the recently executed sequence which is new and unique, and has just been executed
   */
  @SuppressWarnings("PMD.EmptyMethodInAbstractClassShouldBeAbstract")
  public void createdExecutableSequence(ExecutableSequence eSeq) {
    // The default implementation does nothing. Subclasses may override it to add behavior.
  }

  /**
   * A hook that is called after the component set is cleared. It discards any information about
   * sequences that are no longer candidates for selection.
   *
   * <p>The default implementation does nothing. Subclasses may override it to add behavior.
   *
   * @param retainedSequences the sequences that are still candidates for selection: the seed and
   *     literal sequences
   */
  @SuppressWarnings("PMD.EmptyMethodInAbstractClassShouldBeAbstract")
  public void componentsCleared(Set<Sequence> retainedSequences) {
    // The default implementation does nothing. Subclasses may override it to add behavior.
  }
}
