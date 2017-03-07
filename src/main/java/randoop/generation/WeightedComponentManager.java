package randoop.generation;

import randoop.main.GenInputsAbstract;
import randoop.sequence.Sequence;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Weighted component manager implements the needed functionality for weighted sequences
 * and constants.
 */
public class WeightedComponentManager extends ComponentManager {

  private Map<Sequence, Integer> sequenceFrequency;

  public WeightedComponentManager(Collection<Sequence> generalSeeds) {
    super(generalSeeds);
    sequenceFrequency = new LinkedHashMap<>();
  }

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

  public Map<Sequence, Integer> getSequenceFrequency() {
    return sequenceFrequency;
  }
}
