package randoop.generation;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import randoop.main.GenInputsAbstract;
import randoop.sequence.Sequence;

/**
 * WeightedComponentManager extends the functionality of ComponentManager by adding needed
 * functionality for weighted sequences and constants, for the command-line options
 * --weighted-sequences and --weighted-constants.
 */
public class WeightedComponentManager extends ComponentManager {

  /** Sequence frequency represents the number of times a sequence occurs in a set of classes */
  private Map<Sequence, Integer> sequenceFrequency;

  /**
   * Create a component manager, initially populated with the given sequences( which are considered
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
}
