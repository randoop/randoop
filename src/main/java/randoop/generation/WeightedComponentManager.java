package randoop.generation;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import randoop.main.GenInputsAbstract;
import randoop.operation.TypedClassOperation;
import randoop.operation.TypedOperation;
import randoop.sequence.Sequence;
import randoop.types.ClassOrInterfaceType;
import randoop.types.Type;
import randoop.util.Randomness;
import randoop.util.SimpleList;

/**
 * WeightedComponentManager extends the functionality of ComponentManager by adding additional
 * functionality for weighted constants, used by the command-line option <code>--weighted-constants
 * </code>. These additions are tracking a sequence's frequency (number of times it occurs) and
 * retrieving component sequences from the subset of all constant sequences with probability <code>
 * --p-const</code> (otherwise retrieves from all sequences).
 */
public class WeightedComponentManager extends ComponentManager {

  /**
   * Sequence frequency represents the number of times a sequence occurs in a set of classes. Used
   * for the --weighted-constants weighting scheme.
   */
  private Map<Sequence, Integer> sequenceFrequency;

  /**
   * Create a component manager, initially populated with the given sequences (which are considered
   * seed sequences) and with a sequenceFrequency map to support the --weighted-constants
   * command-line option.
   *
   * @param generalSeeds seed sequences. Can be null, in which case the seed sequences set is
   *     considered empty.
   */
  public WeightedComponentManager(Collection<Sequence> generalSeeds) {
    super(generalSeeds);
    sequenceFrequency = new LinkedHashMap<>();
  }

  /**
   * Add a component sequence, and update the sequence's frequency.
   *
   * @param sequence the sequence
   */
  @Override
  public void addGeneratedSequence(Sequence sequence) {
    gralComponents.add(sequence);
    if (GenInputsAbstract.weighted_constants) {
      if (sequenceFrequency.containsKey(sequence)) {
        sequenceFrequency.put(sequence, sequenceFrequency.get(sequence) + 1);
      } else {
        sequenceFrequency.put(sequence, 1);
      }
    }
  }

  /** @return the mapping of sequences to their frequency */
  public Map<Sequence, Integer> getSequenceFrequency() {
    return sequenceFrequency;
  }

  /**
   * Returns component sequences that create values of the type required by the i-th input value of
   * the given statement. Any applicable class- or package-level literals, those are added to the
   * collection as well.
   *
   * @param operation the statement
   * @param i the input value index of statement
   * @return the sequences that create values of the given type
   */
  @SuppressWarnings("unchecked")
  SimpleList<Sequence> getSequencesForType(TypedOperation operation, int i) {
    if (GenInputsAbstract.weighted_constants) {
      return getSequencesForWeightedConstants(operation, i);
    } else {
      return super.getSequencesForType(operation, i);
    }
  }

  /**
   * Returns component sequences that create values of the type required by the i-th input value of
   * the given statement. With probability <code>--p-const</code>, as given by the command-line
   * option, this only returns such component sequences that are weighted constants.
   *
   * @param operation the statement
   * @param i the input value index of statement
   * @return the sequences that create values of the given type for weighted constants
   */
  @SuppressWarnings("unchecked")
  private SimpleList<Sequence> getSequencesForWeightedConstants(TypedOperation operation, int i) {
    Type neededType = operation.getInputTypes().get(i);
    if (Randomness.weightedCoinFlip(GenInputsAbstract.p_const)) {
      ClassOrInterfaceType declaringCls = ((TypedClassOperation) operation).getDeclaringType();
      if (declaringCls != null) {
        if (classLiterals != null) {
          SimpleList<Sequence> sl = classLiterals.getSequences(declaringCls, neededType);
          return sl;
        }
      }
    } else {
      return gralComponents.getSequencesForType(neededType, false);
    }
    return null;
  }
}
